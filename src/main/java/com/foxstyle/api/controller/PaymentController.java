package com.foxstyle.api.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.PayOSLinkResponse;
import com.foxstyle.api.dto.response.PaymentResponse;
import com.foxstyle.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) Byte status,
            Pageable pageable) {
        PageResponse<PaymentResponse> payments = paymentService.getAllPayments(status, pageable);
        ApiResponse<PageResponse<PaymentResponse>> response = ApiResponse.<PageResponse<PaymentResponse>>builder()
                .status("success")
                .message("Lấy danh sách giao dịch thành công")
                .data(payments)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrder(@PathVariable Integer orderId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByOrder(orderId);
        ApiResponse<List<PaymentResponse>> response = ApiResponse.<List<PaymentResponse>>builder()
                .status("success")
                .message("Lấy giao dịch của đơn hàng thành công")
                .data(payments)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Integer paymentId,
            @RequestParam Byte status,
            @RequestParam(required = false) String transactionId) {
        PaymentResponse payment = paymentService.updatePaymentStatus(paymentId, status, transactionId);
        ApiResponse<PaymentResponse> response = ApiResponse.<PaymentResponse>builder()
                .status("success")
                .message("Cập nhật trạng thái giao dịch thành công")
                .data(payment)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{paymentId}/reconciliation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updateReconciliation(
            @PathVariable Integer paymentId,
            @RequestParam boolean reconciled,
            Principal principal) {
        PaymentResponse payment = paymentService.updateReconciliation(
                paymentId, reconciled, principal.getName());
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .status("success")
                .message(reconciled ? "Đối soát giao dịch thành công" : "Đã hủy đối soát giao dịch")
                .data(payment)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/payos/create-link/{orderId}")
    public ResponseEntity<ApiResponse<PayOSLinkResponse>> createPayOSLink(
            @PathVariable Integer orderId,
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String cancelUrl) {
        PayOSLinkResponse payosLink = paymentService.createPayOSPaymentLink(orderId, returnUrl, cancelUrl);
        ApiResponse<PayOSLinkResponse> response = ApiResponse.<PayOSLinkResponse>builder()
                .status("success")
                .message("Khởi tạo link thanh toán PayOS thành công")
                .data(payosLink)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payos/check-status/{orderCode}")
    public ResponseEntity<ApiResponse<PayOSLinkResponse>> checkPayOSStatus(@PathVariable Long orderCode) {
        PayOSLinkResponse statusResponse = paymentService.checkAndSyncPayOSStatus(orderCode);
        ApiResponse<PayOSLinkResponse> response = ApiResponse.<PayOSLinkResponse>builder()
                .status("success")
                .message("Đồng bộ trạng thái PayOS thành công")
                .data(statusResponse)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<ApiResponse<Boolean>> handlePayOSWebhook(@RequestBody ObjectNode webhookBody) {
        boolean success = paymentService.handlePayOSWebhook(webhookBody);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .status("success")
                .message("Xử lý PayOS webhook hoàn tất")
                .data(success)
                .timestamp(LocalDateTime.now())
                .build();
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
