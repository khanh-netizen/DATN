package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.CartItemRequest;
import com.foxstyle.api.dto.request.CheckoutRequest;
import com.foxstyle.api.dto.response.*;
import com.foxstyle.api.entity.*;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.*;
import com.foxstyle.api.service.CouponService;
import com.foxstyle.api.service.OrderService;
import com.foxstyle.api.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal FREE_SHIP_THRESHOLD = BigDecimal.valueOf(300_000);
    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(30_000);
    private static final String PAYMENT_METHOD_COD = "COD";

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductComboItemRepository comboItemRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentReconciliationRepository reconciliationRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final CouponService couponService;
    private final MailService mailService;
    private final SettingRepository settingRepository;

    @Override
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return PageResponse.of(page.map(this::convertToResponse));
    }

    @Override
    public PageResponse<OrderResponse> getMyOrders(String username, Pageable pageable) {
        User user = findUserByUsername(username);
        return PageResponse.of(orderRepository.findByUserUserId(user.getUserId(), pageable)
                .map(this::convertToResponse));
    }

    @Override
    public OrderResponse getOrderById(Integer orderId, String username, boolean isStaff) {
        Order order = findOrderById(orderId);
        if (!isStaff && !order.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Bạn không có quyền xem đơn hàng này");
        }
        return convertToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse checkout(String username, CheckoutRequest request) {
        User user = findUserByUsername(username);
        String paymentMethod = request.getPaymentMethod().trim().toUpperCase();
        if (!PAYMENT_METHOD_COD.equals(paymentMethod)
                && !"TRANSFER".equals(paymentMethod)
                && !"PAYOS".equals(paymentMethod)) {
            throw new BadRequestException("Phương thức thanh toán không hợp lệ");
        }
        if (request.getShippingFee() != null
                && (request.getShippingFee().signum() < 0
                || request.getShippingFee().compareTo(BigDecimal.valueOf(80_000)) > 0)) {
            throw new BadRequestException("Phí vận chuyển không hợp lệ");
        }

        Order order = buildBaseOrder(user, request);
        
        BigDecimal subtotal = processOrderItems(order, request.getItems());
        applyPricing(order, user, subtotal, request.getCouponCode(), request.getShippingFee());

        Order savedOrder = orderRepository.save(order);
        recordCouponUsage(savedOrder);
        // Payment status is only changed after the provider confirms it.
        createPaymentRecord(savedOrder, paymentMethod, false);
        
        boolean isCod = PAYMENT_METHOD_COD.equals(paymentMethod);
        if (isCod) {
            removeCheckedOutItemsFromCart(user, request.getItems());
        }

        // PayOS chưa thanh toán chỉ là yêu cầu thanh toán tạm, chưa xác nhận đặt hàng.
        if (isCod) {
            try {
                final String targetEmail = request.getRecipientEmail();
                new Thread(() -> mailService.sendOrderConfirmationEmail(savedOrder, targetEmail)).start();
            } catch (Exception e) {
                System.err.println("[SMTP] Khởi tạo luồng gửi mail xác nhận đơn hàng thất bại: " + e.getMessage());
            }
        }

        return convertToResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(
            Integer orderId,
            OrderStatus status,
            String reason,
            Boolean warrantyRedelivery) {
        Order order = findOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();

        if (oldStatus == status) {
            return convertToResponse(order);
        }
        if (oldStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Không thể mở lại đơn hàng đã hủy");
        }

        String cleanReason = reason != null ? reason.trim() : "";
        if ((status == OrderStatus.CANCELLED || status == OrderStatus.RETURNED)
                && cleanReason.isEmpty()) {
            throw new BadRequestException(
                    status == OrderStatus.CANCELLED
                            ? "Vui lòng nhập lý do hủy đơn hàng"
                            : "Vui lòng nhập lý do hoàn hàng");
        }

        if (status == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(status);
        if (status == OrderStatus.DELIVERED && order.getDeliveredAt() == null) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        if (status == OrderStatus.DELIVERED) {
            paymentRepository.findByOrderOrderId(orderId).forEach(payment -> {
                syncPaymentWithOrderTotal(payment);
                if ("COD".equalsIgnoreCase(payment.getPaymentMethod())) {
                    payment.setPaymentStatus((byte) 1);
                }
                paymentRepository.save(payment);
            });
        } else if (status == OrderStatus.CANCELLED) {
            paymentRepository.findByOrderOrderId(orderId).forEach(payment -> {
                syncPaymentWithOrderTotal(payment);
                payment.setPaymentStatus((byte) 2);
                paymentRepository.save(payment);
            });
        }
        if (status == OrderStatus.CANCELLED) {
            order.setCancellationReason(cleanReason);
        } else if (status == OrderStatus.RETURNED) {
            order.setReturnReason(cleanReason);
            order.setWarrantyRedelivery(Boolean.TRUE.equals(warrantyRedelivery));
        }
        Order savedOrder = orderRepository.save(order);

        return convertToResponse(savedOrder);
    }


    @Override
    @Transactional
    public OrderResponse cancelMyOrder(String username, Integer orderId, String reason) {
        Order order = findOrderById(orderId);
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập lý do hủy đơn hàng");
        }
        if (!order.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Bạn không có quyền hủy đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestException("Chỉ được hủy đơn hàng khi đơn đang ở trạng thái chờ xử lý hoặc đang xử lý");
        }
        restoreStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason.trim());
        paymentRepository.findByOrderOrderId(orderId).forEach(payment -> {
            syncPaymentWithOrderTotal(payment);
            payment.setPaymentStatus((byte) 2);
            paymentRepository.save(payment);
        });
        return convertToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse requestReturn(
            String username,
            Integer orderId,
            String reason,
            Boolean warrantyRedelivery) {
        Order order = findOrderById(orderId);
        if (!order.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Bạn không có quyền hoàn đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Chỉ được yêu cầu hoàn hàng sau khi đơn đã giao");
        }
        if (order.getDeliveredAt() == null) {
            throw new BadRequestException("Không xác định được thời điểm giao hàng để xử lý yêu cầu hoàn");
        }
        if (LocalDateTime.now().isAfter(order.getDeliveredAt().plusDays(3))) {
            throw new BadRequestException("Đã quá thời hạn hoàn hàng 3 ngày kể từ khi giao thành công");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập lý do hoàn hàng");
        }

        order.setStatus(OrderStatus.RETURNED);
        order.setReturnReason(reason.trim());
        order.setWarrantyRedelivery(Boolean.TRUE.equals(warrantyRedelivery));
        paymentRepository.findByOrderOrderId(orderId).forEach(payment -> {
            syncPaymentWithOrderTotal(payment);
            payment.setPaymentStatus((byte) 2);
            paymentRepository.save(payment);
        });
        return convertToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse dispatchToCarrier(Integer orderId, String carrier) {
        Order order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestException(
                    "Chỉ đơn chờ xử lý hoặc đang xử lý mới được đẩy hãng vận chuyển");
        }
        if (carrier == null || carrier.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng chọn hãng vận chuyển");
        }
        String cleanCarrier = carrier.trim().toUpperCase();
        order.setShippingCarrier(cleanCarrier);
        order.setTrackingCode(
                cleanCarrier.replaceAll("[^A-Z0-9]", "")
                        + "-"
                        + orderId
                        + "-"
                        + System.currentTimeMillis());
        order.setDispatchedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.SHIPPING);
        paymentRepository.findByOrderOrderId(orderId).forEach(payment -> {
            syncPaymentWithOrderTotal(payment);
            paymentRepository.save(payment);
        });
        return convertToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse updateShippingFee(Integer orderId, BigDecimal shippingFee) {
        if (shippingFee == null || shippingFee.signum() < 0) {
            throw new BadRequestException("Phí vận chuyển không hợp lệ");
        }
        Order order = findOrderById(orderId);
        BigDecimal oldFee = order.getShippingFee() != null
                ? order.getShippingFee()
                : BigDecimal.ZERO;
        order.setShippingFee(shippingFee);
        order.setTotalAmount(order.getTotalAmount().subtract(oldFee).add(shippingFee));
        paymentRepository.findByOrderOrderId(orderId).forEach(payment -> {
            syncPaymentWithOrderTotal(payment);
            paymentRepository.save(payment);
        });
        return convertToResponse(orderRepository.save(order));
    }

    // ==================== Private helpers: checkout ====================

    private Order buildBaseOrder(User user, CheckoutRequest request) {
        return Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .shippingAddress(request.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();
    }

    /** Kiểm tra kho, trừ tồn kho và tạo các dòng chi tiết. Trả về tổng tiền hàng. */
    private BigDecimal processOrderItems(Order order, List<CartItemRequest> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();

        for (CartItemRequest item : items) {
            if (item.getComboProductId() != null) {
                subtotal = subtotal.add(processComboItem(order, details, item));
                continue;
            }
            if (item.getVariantId() == null) {
                throw new BadRequestException("Sản phẩm lẻ phải có biến thể màu và size");
            }
            ProductVariant variant = variantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy biến thể sản phẩm có ID: " + item.getVariantId()));

            subtractStock(variant, item.getQuantity());

            BigDecimal price = sellingPrice(variant);
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));

            details.add(OrderDetail.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(item.getQuantity())
                    .price(price)
                    .costPrice(variant.getCostPrice() == null ? BigDecimal.ZERO : variant.getCostPrice())
                    .build());
        }

        order.setOrderDetails(details);
        return subtotal;
    }

    private BigDecimal processComboItem(Order order, List<OrderDetail> details, CartItemRequest item) {
        Product combo = productRepository.findById(item.getComboProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy combo: " + item.getComboProductId()));
        if (!Boolean.TRUE.equals(combo.getIsCombo())) {
            throw new BadRequestException("Sản phẩm được gửi lên không phải combo");
        }
        List<ProductComboItem> configuredItems =
                comboItemRepository.findByComboProductProductIdOrderByDisplayOrderAsc(combo.getProductId());
        if (configuredItems.size() < 2) {
            throw new BadRequestException("Combo chưa được cấu hình đủ sản phẩm thành phần");
        }
        List<Integer> selectedIds = item.getComponentVariantIds() == null
                ? List.of() : item.getComponentVariantIds();
        if (selectedIds.size() != configuredItems.size()) {
            throw new BadRequestException("Phải mua nguyên combo, không được thêm hoặc bỏ món");
        }
        List<ProductVariant> selectedVariants = selectedIds.stream()
                .map(id -> variantRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể: " + id)))
                .toList();

        for (ProductComboItem configured : configuredItems) {
            long matches = selectedVariants.stream().filter(variant ->
                    variant.getProduct().getProductId().equals(configured.getComponentProduct().getProductId())).count();
            if (matches != 1) {
                throw new BadRequestException("Mỗi sản phẩm thành phần phải có đúng một màu-size");
            }
        }
        validateFixedComboVariants(combo, selectedVariants);

        BigDecimal comboPrice = combo.getPrice();
        if (item.getComboVariantId() != null) {
            ProductVariant comboVariant = variantRepository.findById(item.getComboVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể combo"));
            if (!comboVariant.getProduct().getProductId().equals(combo.getProductId())) {
                throw new BadRequestException("Biến thể không thuộc combo đã chọn");
            }
            comboPrice = sellingPrice(comboVariant);
        }
        List<ProductVariant> paidVariants = new ArrayList<>();
        for (ProductComboItem configured : configuredItems) {
            if (!Boolean.TRUE.equals(configured.getIsGift())) {
                selectedVariants.stream().filter(v -> v.getProduct().getProductId()
                        .equals(configured.getComponentProduct().getProductId())).findFirst()
                        .ifPresent(paidVariants::add);
            }
        }
        BigDecimal paidReferenceTotal = paidVariants.stream()
                .map(this::sellingPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        for (ProductComboItem configured : configuredItems) {
            ProductVariant variant = selectedVariants.stream().filter(v -> v.getProduct().getProductId()
                    .equals(configured.getComponentProduct().getProductId())).findFirst().orElseThrow();
            int componentQuantity = Math.multiplyExact(item.getQuantity(), configured.getQuantity());
            subtractStock(variant, componentQuantity);
            BigDecimal allocatedPrice = Boolean.TRUE.equals(configured.getIsGift()) || paidReferenceTotal.signum() == 0
                    ? BigDecimal.ZERO
                    : comboPrice.multiply(sellingPrice(variant))
                            .divide(paidReferenceTotal, 2, RoundingMode.HALF_UP);
            details.add(OrderDetail.builder().order(order).variant(variant)
                    .quantity(componentQuantity).price(allocatedPrice)
                    .costPrice(variant.getCostPrice() == null ? BigDecimal.ZERO : variant.getCostPrice()).build());
        }
        return comboPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private BigDecimal sellingPrice(ProductVariant variant) {
        Product product = variant.getProduct();
        if (product.getFlashSaleStartAt() != null && product.getFlashSaleEndAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (!now.isBefore(product.getFlashSaleStartAt()) && now.isBefore(product.getFlashSaleEndAt())) {
                return product.getPrice();
            }
            if (product.getOriginalPrice() != null) {
                return product.getOriginalPrice();
            }
        }
        return variant.getPrice() != null ? variant.getPrice() : variant.getProduct().getPrice();
    }

    private void validateFixedComboVariants(Product combo, List<ProductVariant> selectedVariants) {
        String description = combo.getDescription() == null ? "" : combo.getDescription();
        if (!description.contains("[COMBO_MODE:FIXED]")) return;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\[COMBO_VARIANTS:([^\\]]+)]").matcher(description);
        if (!matcher.find()) throw new BadRequestException("Combo cố định chưa cấu hình màu-size");
        java.util.Set<Integer> required = java.util.Arrays.stream(matcher.group(1).split(","))
                .map(value -> value.split("=")).filter(parts -> parts.length == 2)
                .map(parts -> Integer.valueOf(parts[1])).collect(java.util.stream.Collectors.toSet());
        java.util.Set<Integer> selected = selectedVariants.stream()
                .map(ProductVariant::getVariantId).collect(java.util.stream.Collectors.toSet());
        if (!required.equals(selected)) {
            throw new BadRequestException("Không được thay đổi màu-size của combo cố định");
        }
    }

    private void subtractStock(ProductVariant variant, int quantity) {
        if (variant.getQuantity() < quantity) {
            throw new BadRequestException(String.format(
                    "Sản phẩm màu %s size %s không đủ số lượng trong kho (còn %d)",
                    variant.getColor(), variant.getSize(), variant.getQuantity()));
        }
        variant.setQuantity(variant.getQuantity() - quantity);
        variantRepository.save(variant);
    }

    private void applyPricing(Order order, User user, BigDecimal subtotal, String couponCode, BigDecimal requestedShippingFee) {
        BigDecimal discount = BigDecimal.ZERO;

        if (StringUtils.hasText(couponCode)) {
            discount = couponService.validateAndCalculateDiscount(couponCode, subtotal, user.getUserId());
            Coupon coupon = couponRepository.findByCouponCode(couponCode.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại: " + couponCode));
            order.setCoupon(coupon);
        }

        BigDecimal completedSpending = orderRepository.sumTotalAmountByUserAndStatus(
                user.getUserId(), OrderStatus.DELIVERED);
        int memberDiscountPercent = completedSpending.compareTo(BigDecimal.valueOf(10_000_000)) > 0
                ? 8
                : completedSpending.compareTo(BigDecimal.valueOf(2_000_000)) >= 0 ? 5 : 3;
        BigDecimal memberDiscount = subtotal
                    .multiply(BigDecimal.valueOf(memberDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        discount = discount.add(memberDiscount).min(subtotal);

        BigDecimal shippingFee;
        if (completedSpending.compareTo(BigDecimal.valueOf(10_000_000)) > 0
                || subtotal.compareTo(FREE_SHIP_THRESHOLD) >= 0) {
            shippingFee = BigDecimal.ZERO;
        } else if (requestedShippingFee != null && requestedShippingFee.compareTo(BigDecimal.ZERO) >= 0) {
            shippingFee = requestedShippingFee;
        } else {
            shippingFee = DEFAULT_SHIPPING_FEE;
        }

        BigDecimal taxableAmount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal taxRate = settingRepository.findBySettingKey("vat_tax_rate")
                .map(Setting::getSettingValue)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(BigDecimal::new)
                .orElse(BigDecimal.valueOf(8));
        BigDecimal taxAmount = taxableAmount.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = taxableAmount.add(taxAmount).add(shippingFee);

        order.setDiscountAmount(discount);
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(total);
    }

    /** Ghi vết user đã dùng coupon và tăng bộ đếm sử dụng. */
    private void recordCouponUsage(Order order) {
        if (order.getCoupon() == null) {
            return;
        }
        Coupon coupon = order.getCoupon();
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        UserCoupon userCoupon = UserCoupon.builder()
                .id(new UserCouponId(order.getUser().getUserId(), coupon.getCouponId()))
                .user(order.getUser())
                .coupon(coupon)
                .order(order)
                .usedAt(LocalDateTime.now())
                .build();
        userCouponRepository.save(userCoupon);
    }

    private void createPaymentRecord(Order order, String paymentMethod, Boolean isPaid) {
        Payment payment = paymentRepository.findTopByOrderOrderIdOrderByPaymentIdDesc(order.getOrderId())
                .orElseGet(Payment::new);
        payment.setOrder(order);
        payment.setPaymentMethod(StringUtils.hasText(paymentMethod) ? paymentMethod : PAYMENT_METHOD_COD);
        payment.setPaymentStatus((byte) (isPaid != null && isPaid ? 1 : 0)); // 1 = Đã thanh toán, 0 = Chưa thanh toán
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(order.getTotalAmount());
        paymentRepository.save(payment);
    }

    /** Sau khi đặt hàng thành công, gỡ các biến thể đã mua ra khỏi giỏ hàng. */
    private void removeCheckedOutItemsFromCart(User user, List<CartItemRequest> items) {
        cartRepository.findByUserUserId(user.getUserId()).ifPresent(cart ->
                items.forEach(item ->
                        cartDetailRepository
                                .findByCartCartIdAndVariantVariantId(cart.getCartId(), item.getVariantId())
                                .ifPresent(cartDetailRepository::delete)));
    }

    private void restoreStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductVariant variant = detail.getVariant();
            variant.setQuantity(variant.getQuantity() + detail.getQuantity());
            variantRepository.save(variant);
        }
    }

    // ==================== Private helpers: chung ====================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + username));
    }

    private Order findOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng có ID: " + orderId));
    }

    private OrderResponse convertToResponse(Order order) {
        List<OrderDetailResponse> details = order.getOrderDetails() != null
                ? order.getOrderDetails().stream().map(this::convertDetail).toList()
                : List.of();

        List<PaymentResponse> payments = paymentRepository.findByOrderOrderId(order.getOrderId())
                .stream()
                .map(this::convertPayment)
                .toList();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .customerName(order.getUser().getFullName())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .cancellationReason(order.getCancellationReason())
                .returnReason(order.getReturnReason())
                .warrantyRedelivery(order.getWarrantyRedelivery())
                .deliveredAt(order.getDeliveredAt())
                .shippingCarrier(order.getShippingCarrier())
                .trackingCode(order.getTrackingCode())
                .dispatchedAt(order.getDispatchedAt())
                .couponCode(order.getCoupon() != null ? order.getCoupon().getCouponCode() : null)
                .details(details)
                .payments(payments)
                .build();
    }

    private OrderDetailResponse convertDetail(OrderDetail detail) {
        ProductVariant variant = detail.getVariant();
        Product product = variant.getProduct();
        return OrderDetailResponse.builder()
                .orderDetailId(detail.getOrderDetailId())
                .variantId(variant.getVariantId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .imageUrl(product.getImageUrl())
                .color(variant.getColor())
                .size(variant.getSize())
                .quantity(detail.getQuantity())
                .price(detail.getPrice())
                .lineTotal(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .build();
    }

    private PaymentResponse convertPayment(Payment payment) {
        if (syncPaymentWithOrderTotal(payment)) {
            paymentRepository.save(payment);
        }
        PaymentReconciliation reconciliation = reconciliationRepository
                .findByPaymentPaymentId(payment.getPaymentId())
                .orElse(null);
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .reconciled(reconciliation != null)
                .reconciliationCode(
                        reconciliation != null ? reconciliation.getReconciliationCode() : null)
                .reconciledAt(
                        reconciliation != null ? reconciliation.getReconciledAt() : null)
                .reconciledBy(
                        reconciliation != null && reconciliation.getReconciledBy() != null
                                ? reconciliation.getReconciledBy().getUsername()
                                : null)
                .build();
    }

    private boolean syncPaymentWithOrderTotal(Payment payment) {
        if (payment.getOrder() == null || payment.getOrder().getTotalAmount() == null) {
            return false;
        }
        if (payment.getAmount() == null
                || payment.getAmount().compareTo(payment.getOrder().getTotalAmount()) != 0) {
            payment.setAmount(payment.getOrder().getTotalAmount());
            return true;
        }
        return false;
    }
}
