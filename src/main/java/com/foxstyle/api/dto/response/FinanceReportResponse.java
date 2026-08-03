package com.foxstyle.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FinanceReportResponse {
    private BigDecimal revenue;
    private BigDecimal costOfGoodsSold;
    private BigDecimal grossProfit;
    private BigDecimal stockImportCost;
    private BigDecimal inventoryValue;
    private List<PeriodRow> periods;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PeriodRow {
        private String period;
        private BigDecimal revenue;
        private BigDecimal costOfGoodsSold;
        private BigDecimal grossProfit;
        private BigDecimal stockImportCost;
        private Long orderCount;
    }
}
