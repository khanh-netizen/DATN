package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockImportRequest {
    @NotNull private Integer variantId;
    @NotNull @Min(1) private Integer quantity;
    @NotNull @DecimalMin("0.01") private BigDecimal unitCost;
}
