package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.StockImportRequest;
import com.foxstyle.api.dto.response.FinanceReportResponse;
import com.foxstyle.api.dto.request.StockImportReceiptRequest;
import com.foxstyle.api.dto.response.StockImportReceiptResponse;
import java.util.List;

public interface FinanceReportService {
    void recordStockImport(StockImportRequest request);
    FinanceReportResponse getReport(String period);
    StockImportReceiptResponse createReceipt(StockImportReceiptRequest request, String username);
    List<StockImportReceiptResponse> getReceipts();
}
