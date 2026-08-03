package com.foxstyle.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.foxstyle.api.config.PayOSConfig;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.PayOSLinkResponse;
import com.foxstyle.api.dto.response.PaymentResponse;
import com.foxstyle.api.entity.Order;
import com.foxstyle.api.entity.OrderDetail;
import com.foxstyle.api.entity.OrderStatus;
import com.foxstyle.api.entity.Payment;
import com.foxstyle.api.entity.PaymentReconciliation;
import com.foxstyle.api.entity.User;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.CartDetailRepository;
import com.foxstyle.api.repository.CartRepository;
import com.foxstyle.api.repository.OrderRepository;
import com.foxstyle.api.repository.PaymentRepository;
import com.foxstyle.api.repository.PaymentReconciliationRepository;
import com.foxstyle.api.repository.UserRepository;
import com.foxstyle.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYOS_CREATE_URL = "https://api-merchant.payos.vn/v2/payment-requests";
    private static final String PAYOS_QUERY_URL = "https://api-merchant.payos.vn/v2/payment-requests/";

    private final PaymentRepository paymentRepository;
    private final PaymentReconciliationRepository reconciliationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final PayOSConfig payOSConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public PageResponse<PaymentResponse> getAllPayments(Byte paymentStatus, Pageable pageable) {
        Page<Payment> page = paymentRepository.findLatestPerOrder(paymentStatus, pageable);
        return PageResponse.of(page.map(this::convertToResponse));
    }

    @Override
    @Transactional
    public List<PaymentResponse> getPaymentsByOrder(Integer orderId) {
        return paymentRepository.findByOrderOrderIdOrderByPaymentIdDesc(orderId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Integer paymentId, Byte paymentStatus, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán có ID: " + paymentId));
        payment.setPaymentStatus(paymentStatus);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        return convertToResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse updateReconciliation(
            Integer paymentId,
            boolean reconciled,
            String username) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy giao dịch thanh toán có ID: " + paymentId));

        if (reconciled) {
            if (payment.getPaymentStatus() != 1) {
                throw new BadRequestException(
                        "Chỉ được đối soát giao dịch đã thanh toán thành công");
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tài khoản quản trị"));
            reconciliationRepository.findByPaymentPaymentId(paymentId)
                    .orElseGet(() -> reconciliationRepository.save(
                            PaymentReconciliation.builder()
                                    .payment(payment)
                                    .reconciledBy(user)
                                    .reconciliationCode(
                                            "RECON-"
                                                    + LocalDateTime.now().format(
                                                            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                                                    + "-"
                                                    + paymentId)
                                    .build()));
        } else {
            reconciliationRepository.findByPaymentPaymentId(paymentId)
                    .ifPresent(reconciliationRepository::delete);
        }
        return convertToResponse(payment);
    }

    @Override
    @Transactional
    public PayOSLinkResponse createPayOSPaymentLink(Integer orderId, String returnUrl, String cancelUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));

        long orderCode = Long.parseLong(String.valueOf(order.getOrderId()));
        int amount = order.getTotalAmount().intValue();
        String description = "DH" + order.getOrderId();
        String finalReturnUrl = (returnUrl != null && !returnUrl.isBlank()) ? returnUrl : "http://localhost:5173/orders";
        String finalCancelUrl = (cancelUrl != null && !cancelUrl.isBlank()) ? cancelUrl : "http://localhost:5173/checkout";

        // Signature data format sorted alphabetically: amount, cancelUrl, description, orderCode, returnUrl
        String signatureData = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                amount, finalCancelUrl, description, orderCode, finalReturnUrl);
        String signature = calculateHmacSHA256(signatureData, payOSConfig.getChecksumKey());

        Map<String, Object> body = new HashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", amount);
        body.put("description", description);
        body.put("cancelUrl", finalCancelUrl);
        body.put("returnUrl", finalReturnUrl);
        body.put("signature", signature);

        HttpHeaders headers = createPayOSHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(PAYOS_CREATE_URL, entity, JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode resData = response.getBody().get("data");
                if (resData != null) {
                    return parsePayOSResponse(orderId, orderCode, order.getTotalAmount(), resData);
                }
            }
        } catch (Exception e) {
            System.err.println("[PayOS API Create Link Warning] " + e.getMessage() + ". Attempting status query fallback.");
        }

        // Fallback: If link already created or direct query needed
        return checkAndSyncPayOSStatus(orderCode);
    }

    @Override
    @Transactional
    public PayOSLinkResponse checkAndSyncPayOSStatus(Long orderCode) {
        Integer orderId = orderCode.intValue();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));

        String status = "PENDING";
        String qrCode = null;
        String checkoutUrl = null;
        String accountName = "NGUYEN TAN NGUYEN CHIEN";
        String accountNumber = "0362804559";
        String bin = "970422";
        String paymentLinkId = null;
        java.math.BigDecimal paidAmount = null;

        try {
            HttpHeaders headers = createPayOSHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    PAYOS_QUERY_URL + orderCode,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode dataNode = response.getBody().get("data");
                if (dataNode != null) {
                    status = dataNode.path("status").asText("PENDING");
                    qrCode = dataNode.path("qrCode").asText(null);
                    checkoutUrl = dataNode.path("checkoutUrl").asText(null);
                    accountName = dataNode.path("accountName").asText(accountName);
                    accountNumber = dataNode.path("accountNumber").asText(accountNumber);
                    bin = dataNode.path("bin").asText(bin);
                    paymentLinkId = dataNode.path("id").asText(null);
                    if (dataNode.has("amount") && dataNode.get("amount").isNumber()) {
                        paidAmount = dataNode.get("amount").decimalValue();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[PayOS API Query Error] " + e.getMessage());
        }

        if ("PAID".equalsIgnoreCase(status)
                && paidAmount != null
                && paidAmount.compareTo(order.getTotalAmount()) == 0) {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
            }
            final String syncedPaymentLinkId = paymentLinkId;
            final Long syncedOrderCode = orderCode;
            paymentRepository.findTopByOrderOrderIdOrderByPaymentIdDesc(orderId).ifPresent(payment -> {
                payment.setPaymentStatus((byte) 1);
                if (syncedPaymentLinkId != null) {
                    payment.setTransactionId(syncedPaymentLinkId);
                } else if (payment.getTransactionId() == null) {
                    payment.setTransactionId("PAYOS-" + syncedOrderCode);
                }
                paymentRepository.save(payment);
            });
            clearUserCartForOrder(order);
        }

        if (qrCode == null) {
            qrCode = String.format("https://img.vietqr.io/image/MB-0362804559-qr_only.png?amount=%d&addInfo=DH%d&accountName=NGUYEN%%20TAN%%20NGUYEN%%20CHIEN",
                    order.getTotalAmount().intValue(), orderId);
        }

        return PayOSLinkResponse.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .checkoutUrl(checkoutUrl)
                .qrCode(qrCode)
                .amount(order.getTotalAmount())
                .status(status)
                .paymentLinkId(paymentLinkId)
                .accountName(accountName)
                .accountNumber(accountNumber)
                .bin(bin)
                .description("DH" + orderId)
                .build();
    }

    @Override
    @Transactional
    public boolean handlePayOSWebhook(ObjectNode webhookBody) {
        try {
            if (!isValidWebhookSignature(webhookBody)) {
                System.err.println("[PayOS Webhook] Chữ ký không hợp lệ");
                return false;
            }
            if (webhookBody.path("success").asBoolean(false) && webhookBody.has("data")) {
                JsonNode data = webhookBody.get("data");
                if (data.has("orderCode")) {
                    Long orderCode = data.get("orderCode").asLong();
                    checkAndSyncPayOSStatus(orderCode);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[PayOS Webhook Exception] " + e.getMessage());
        }
        return false;
    }

    private boolean isValidWebhookSignature(ObjectNode webhookBody) {
        JsonNode data = webhookBody.get("data");
        String received = webhookBody.path("signature").asText("");
        if (data == null || !data.isObject() || received.isBlank()) return false;
        java.util.List<String> keys = new java.util.ArrayList<>();
        data.fieldNames().forEachRemaining(keys::add);
        java.util.Collections.sort(keys);
        String canonical = keys.stream().map(key -> {
            JsonNode value = data.get(key);
            String text;
            if (value == null || value.isNull()) text = "";
            else if (value.isValueNode()) text = value.asText();
            else {
                try { text = objectMapper.writeValueAsString(value); }
                catch (Exception ex) { throw new IllegalArgumentException(ex); }
            }
            return key + "=" + text;
        }).collect(java.util.stream.Collectors.joining("&"));
        String expected = calculateHmacSHA256(canonical, payOSConfig.getChecksumKey());
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                received.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    private HttpHeaders createPayOSHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payOSConfig.getClientId());
        headers.set("x-api-key", payOSConfig.getApiKey());
        return headers;
    }

    private PayOSLinkResponse parsePayOSResponse(Integer orderId, Long orderCode, java.math.BigDecimal amount, JsonNode data) {
        String qrCode = data.path("qrCode").asText();
        if (qrCode == null || qrCode.isBlank()) {
            qrCode = String.format("https://img.vietqr.io/image/MB-0362804559-qr_only.png?amount=%d&addInfo=DH%d&accountName=NGUYEN%%20TAN%%20NGUYEN%%20CHIEN",
                    amount.intValue(), orderId);
        }

        return PayOSLinkResponse.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .checkoutUrl(data.path("checkoutUrl").asText(null))
                .qrCode(qrCode)
                .amount(amount)
                .status(data.path("status").asText("PENDING"))
                .paymentLinkId(data.path("paymentLinkId").asText(null))
                .accountName(data.path("accountName").asText("NGUYEN TAN NGUYEN CHIEN"))
                .accountNumber(data.path("accountNumber").asText("0362804559"))
                .bin(data.path("bin").asText("970422"))
                .description("DH" + orderId)
                .build();
    }

    private void clearUserCartForOrder(Order order) {
        try {
            if (order.getUser() != null) {
                cartRepository.findByUserUserId(order.getUser().getUserId()).ifPresent(cart -> {
                    if (order.getOrderDetails() != null) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                            cartDetailRepository
                                    .findByCartCartIdAndVariantVariantId(cart.getCartId(), detail.getVariant().getVariantId())
                                    .ifPresent(cartDetailRepository::delete);
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[PayOS] Lỗi khi dọn dẹp giỏ hàng: " + e.getMessage());
        }
    }

    private String calculateHmacSHA256(String data, String key) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKey);
            byte[] bytes = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new BadRequestException("Lỗi khởi tạo chữ ký HMAC SHA256: " + e.getMessage());
        }
    }

    private PaymentResponse convertToResponse(Payment payment) {
        syncAmountWithOrder(payment);
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

    private void syncAmountWithOrder(Payment payment) {
        if (payment.getOrder() == null || payment.getOrder().getTotalAmount() == null) {
            return;
        }
        if (payment.getAmount() == null
                || payment.getAmount().compareTo(payment.getOrder().getTotalAmount()) != 0) {
            payment.setAmount(payment.getOrder().getTotalAmount());
            paymentRepository.save(payment);
        }
    }
}
