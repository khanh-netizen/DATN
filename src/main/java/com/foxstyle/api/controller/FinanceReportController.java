package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.StockImportRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.FinanceReportResponse;
import com.foxstyle.api.dto.request.StockImportReceiptRequest;
import com.foxstyle.api.dto.response.StockImportReceiptResponse;
import com.foxstyle.api.service.FinanceReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1")
public class FinanceReportController {
    private final FinanceReportService financeReportService;

    @PostMapping("/stock-imports")
    public ResponseEntity<ApiResponse<Void>> record(@Valid @RequestBody StockImportRequest request) {
        financeReportService.recordStockImport(request);
        return ResponseEntity.ok(response(null, "Đã ghi nhận tiền nhập kho"));
    }

    @GetMapping("/reports/finance")
    public ResponseEntity<ApiResponse<FinanceReportResponse>> report(
            @RequestParam(defaultValue = "month") String period) {
        return ResponseEntity.ok(response(financeReportService.getReport(period),
                "Lấy báo cáo tài chính thành công"));
    }

    @PostMapping("/stock-import-receipts")
    public ResponseEntity<ApiResponse<StockImportReceiptResponse>> createReceipt(
            @Valid @RequestBody StockImportReceiptRequest request, Principal principal) {
        return ResponseEntity.ok(response(
                financeReportService.createReceipt(request, principal.getName()),
                "Tạo phiếu nhập kho thành công"));
    }

    @GetMapping("/stock-import-receipts")
    public ResponseEntity<ApiResponse<List<StockImportReceiptResponse>>> receipts() {
        return ResponseEntity.ok(response(financeReportService.getReceipts(),
                "Lấy danh sách phiếu nhập kho thành công"));
    }

    private <T> ApiResponse<T> response(T data, String message) {
        return ApiResponse.<T>builder().status("success").message(message)
                .data(data).timestamp(LocalDateTime.now()).build();
    }
}
