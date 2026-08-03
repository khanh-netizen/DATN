package com.foxstyle.api.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.PayOSLinkResponse;
import com.foxstyle.api.dto.response.PaymentResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PaymentService {

    PageResponse<PaymentResponse> getAllPayments(Byte paymentStatus, Pageable pageable);

    List<PaymentResponse> getPaymentsByOrder(Integer orderId);

    /** Cập nhật kết quả giao dịch từ cổng thanh toán. */
    PaymentResponse updatePaymentStatus(Integer paymentId, Byte paymentStatus, String transactionId);

    PaymentResponse updateReconciliation(
            Integer paymentId,
            boolean reconciled,
            String username);

    /** Tạo link & mã QR thanh toán PayOS cho đơn hàng */
    PayOSLinkResponse createPayOSPaymentLink(Integer orderId, String returnUrl, String cancelUrl);

    /** Kiểm tra & đồng bộ trạng thái thanh toán từ PayOS SDK */
    PayOSLinkResponse checkAndSyncPayOSStatus(Long orderCode);

    /** Nhận webhook thông báo giao dịch từ PayOS */
    boolean handlePayOSWebhook(ObjectNode webhookBody);
}

