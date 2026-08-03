package com.foxstyle.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockImportReceiptResponse {
    private Long receiptId;
    private String receiptCode;
    private String supplierName;
    private String supplierPhone;
    private String note;
    private BigDecimal totalAmount;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal otherFee;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<Item> items;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Item {
        private Integer variantId;
        private String productName;
        private String sku;
        private String color;
        private String size;
        private Integer quantity;
        private Integer stockAfter;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }
}
