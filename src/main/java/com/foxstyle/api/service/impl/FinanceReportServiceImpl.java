package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.StockImportRequest;
import com.foxstyle.api.dto.response.FinanceReportResponse;
import com.foxstyle.api.dto.request.StockImportReceiptRequest;
import com.foxstyle.api.dto.response.StockImportReceiptResponse;
import com.foxstyle.api.entity.ProductVariant;
import com.foxstyle.api.entity.StockImport;
import com.foxstyle.api.entity.StockImportReceipt;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.ProductVariantRepository;
import com.foxstyle.api.repository.StockImportRepository;
import com.foxstyle.api.repository.StockImportReceiptRepository;
import com.foxstyle.api.service.FinanceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinanceReportServiceImpl implements FinanceReportService {
    private final StockImportRepository stockImportRepository;
    private final ProductVariantRepository variantRepository;
    private final JdbcTemplate jdbcTemplate;
    private final StockImportReceiptRepository receiptRepository;

    @Override
    @Transactional
    public StockImportReceiptResponse createReceipt(StockImportReceiptRequest request, String username) {
        BigDecimal subtotal = request.getItems().stream()
                .map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = nonNegative(request.getDiscountAmount());
        BigDecimal shipping = nonNegative(request.getShippingFee());
        BigDecimal otherFee = nonNegative(request.getOtherFee());
        BigDecimal taxRate = nonNegative(request.getTaxRate());
        if (discount.compareTo(subtotal) > 0) {
            throw new com.foxstyle.api.exception.BadRequestException(
                    "Chiết khấu nhập kho không được lớn hơn tiền hàng");
        }
        BigDecimal taxableAmount = subtotal.subtract(discount);
        BigDecimal taxAmount = taxableAmount.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = taxableAmount.add(taxAmount).add(shipping).add(otherFee);
        BigDecimal landedCostRatio = totalPayable.divide(subtotal, 12, RoundingMode.HALF_UP);

        StockImportReceipt receipt = StockImportReceipt.builder()
                .receiptCode("PN-" + java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")))
                .supplierName(request.getSupplierName().trim())
                .supplierPhone(request.getSupplierPhone())
                .note(request.getNote())
                .subtotalAmount(subtotal).discountAmount(discount)
                .shippingFee(shipping).otherFee(otherFee)
                .taxRate(taxRate).taxAmount(taxAmount)
                .totalAmount(totalPayable)
                .createdBy(username)
                .build();
        receiptRepository.save(receipt);
        java.util.Set<Integer> variantIds = new java.util.HashSet<>();
        for (StockImportReceiptRequest.Item item : request.getItems()) {
            if (!variantIds.add(item.getVariantId())) {
                throw new com.foxstyle.api.exception.BadRequestException(
                        "Một biến thể màu-size chỉ được xuất hiện một lần trong phiếu nhập");
            }
            ProductVariant variant = variantRepository.findByIdForUpdate(item.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy biến thể: " + item.getVariantId()));
            int stockBefore = variant.getQuantity();
            int stockAfter = Math.addExact(stockBefore, item.getQuantity());
            BigDecimal landedUnitCost = item.getUnitCost().multiply(landedCostRatio)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal averageCost = calculateWeightedAverageCost(
                    stockBefore, variant.getCostPrice(), item.getQuantity(), landedUnitCost);
            variant.setQuantity(stockAfter);
            variant.setCostPrice(averageCost);
            variantRepository.save(variant);
            BigDecimal lineTotal = landedUnitCost.multiply(BigDecimal.valueOf(item.getQuantity()));
            StockImport stockImport = StockImport.builder().receipt(receipt).variant(variant)
                    .quantity(item.getQuantity()).unitCost(landedUnitCost)
                    .totalCost(lineTotal).stockAfter(stockAfter).build();
            stockImportRepository.save(stockImport);
            receipt.getItems().add(stockImport);
        }
        return toReceiptResponse(receiptRepository.save(receipt));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockImportReceiptResponse> getReceipts() {
        return receiptRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toReceiptResponse).toList();
    }

    @Override
    @Transactional
    public void recordStockImport(StockImportRequest request) {
        ProductVariant variant = variantRepository.findByIdForUpdate(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy biến thể: " + request.getVariantId()));
        int stockBefore = variant.getQuantity();
        int stockAfter = Math.addExact(stockBefore, request.getQuantity());
        BigDecimal averageCost = calculateWeightedAverageCost(
                stockBefore, variant.getCostPrice(), request.getQuantity(), request.getUnitCost());
        variant.setQuantity(stockAfter);
        variant.setCostPrice(averageCost);
        variantRepository.save(variant);
        stockImportRepository.save(StockImport.builder()
                .variant(variant)
                .quantity(request.getQuantity())
                .unitCost(request.getUnitCost())
                .totalCost(request.getUnitCost().multiply(BigDecimal.valueOf(request.getQuantity())))
                .stockAfter(stockAfter)
                .build());
    }

    private BigDecimal calculateWeightedAverageCost(
            int stockBefore, BigDecimal currentCost, int importedQuantity, BigDecimal importCost) {
        if (stockBefore <= 0 || currentCost == null || currentCost.signum() <= 0) {
            return importCost.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal currentInventoryValue = currentCost.multiply(BigDecimal.valueOf(stockBefore));
        BigDecimal importedValue = importCost.multiply(BigDecimal.valueOf(importedQuantity));
        return currentInventoryValue.add(importedValue)
                .divide(BigDecimal.valueOf((long) stockBefore + importedQuantity), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceReportResponse getReport(String period) {
        boolean weekly = "week".equalsIgnoreCase(period);
        boolean daily = "day".equalsIgnoreCase(period);
        String saleDate = "COALESCE(o.delivered_at,o.order_date)";
        String orderPeriod = daily
                ? "CONVERT(VARCHAR(10)," + saleDate + ",23)"
                : weekly
                    ? "CONCAT(YEAR(" + saleDate + "), '-W', RIGHT('0' + CAST(DATEPART(ISO_WEEK," + saleDate + ") AS VARCHAR),2))"
                    : "FORMAT(" + saleDate + ", 'yyyy-MM')";
        String importPeriod = daily
                ? "CONVERT(VARCHAR(10),si.imported_at,23)"
                : weekly
                    ? "CONCAT(YEAR(si.imported_at), '-W', RIGHT('0' + CAST(DATEPART(ISO_WEEK,si.imported_at) AS VARCHAR),2))"
                    : "FORMAT(si.imported_at, 'yyyy-MM')";

        String salesSql = "SELECT period_key, SUM(order_revenue) revenue, COUNT(*) order_count, " +
                "SUM(order_cogs) cogs FROM (SELECT o.order_id, " + orderPeriod +
                " period_key, (o.total_amount - COALESCE(o.tax_amount,0) - COALESCE(o.shipping_fee,0)) order_revenue, " +
                "SUM(od.quantity * COALESCE(od.cost_price,pv.cost_price,0)) order_cogs " +
                "FROM orders o JOIN order_details od ON od.order_id=o.order_id " +
                "JOIN product_variants pv ON pv.variant_id=od.variant_id " +
                "WHERE o.status=3 GROUP BY o.order_id, o.total_amount, o.tax_amount, o.shipping_fee, " + orderPeriod +
                ") sales GROUP BY period_key";
        String importsSql = "SELECT " + importPeriod + " period_key, SUM(si.total_cost) import_cost " +
                "FROM stock_imports si GROUP BY " + importPeriod;

        Map<String, FinanceReportResponse.PeriodRow> rows = new TreeMap<>();
        jdbcTemplate.queryForList(salesSql).forEach(row -> {
            BigDecimal revenue = decimal(row.get("revenue"));
            BigDecimal cogs = decimal(row.get("cogs"));
            rows.put(String.valueOf(row.get("period_key")), FinanceReportResponse.PeriodRow.builder()
                    .period(String.valueOf(row.get("period_key")))
                    .revenue(revenue).costOfGoodsSold(cogs)
                    .grossProfit(revenue.subtract(cogs))
                    .stockImportCost(BigDecimal.ZERO)
                    .orderCount(((Number) row.get("order_count")).longValue()).build());
        });
        jdbcTemplate.queryForList(importsSql).forEach(row -> {
            String key = String.valueOf(row.get("period_key"));
            FinanceReportResponse.PeriodRow item = rows.computeIfAbsent(key, k ->
                    FinanceReportResponse.PeriodRow.builder().period(k)
                            .revenue(BigDecimal.ZERO).costOfGoodsSold(BigDecimal.ZERO)
                            .grossProfit(BigDecimal.ZERO).stockImportCost(BigDecimal.ZERO)
                            .orderCount(0L).build());
            item.setStockImportCost(decimal(row.get("import_cost")));
        });

        BigDecimal revenue = rows.values().stream().map(FinanceReportResponse.PeriodRow::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cogs = rows.values().stream().map(FinanceReportResponse.PeriodRow::getCostOfGoodsSold)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal importCost = rows.values().stream().map(FinanceReportResponse.PeriodRow::getStockImportCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal inventoryValue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(CAST(quantity AS DECIMAL(18,2)) * COALESCE(cost_price, 0)), 0) " +
                        "FROM product_variants WHERE quantity > 0",
                BigDecimal.class);
        return FinanceReportResponse.builder().revenue(revenue).costOfGoodsSold(cogs)
                .grossProfit(revenue.subtract(cogs)).stockImportCost(importCost)
                .inventoryValue(inventoryValue == null ? BigDecimal.ZERO : inventoryValue)
                .periods(new ArrayList<>(rows.values())).build();
    }

    private BigDecimal decimal(Object value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    private BigDecimal nonNegative(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.max(BigDecimal.ZERO);
    }

    private StockImportReceiptResponse toReceiptResponse(StockImportReceipt receipt) {
        List<StockImportReceiptResponse.Item> items = receipt.getItems().stream().map(item -> {
            ProductVariant variant = item.getVariant();
            return StockImportReceiptResponse.Item.builder()
                    .variantId(variant.getVariantId())
                    .productName(variant.getProduct().getProductName())
                    .sku(variant.getSku()).color(variant.getColor()).size(variant.getSize())
                    .quantity(item.getQuantity()).stockAfter(item.getStockAfter())
                    .unitCost(item.getUnitCost())
                    .totalCost(item.getTotalCost()).build();
        }).toList();
        return StockImportReceiptResponse.builder()
                .receiptId(receipt.getReceiptId()).receiptCode(receipt.getReceiptCode())
                .supplierName(receipt.getSupplierName()).supplierPhone(receipt.getSupplierPhone())
                .note(receipt.getNote()).totalAmount(receipt.getTotalAmount())
                .subtotalAmount(receipt.getSubtotalAmount())
                .discountAmount(receipt.getDiscountAmount())
                .shippingFee(receipt.getShippingFee()).otherFee(receipt.getOtherFee())
                .taxRate(receipt.getTaxRate()).taxAmount(receipt.getTaxAmount())
                .createdBy(receipt.getCreatedBy()).createdAt(receipt.getCreatedAt())
                .items(items).build();
    }
}
