package com.foxstyle.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StockImportReceiptRequest {
    @NotBlank private String supplierName;
    private String supplierPhone;
    private String note;
    @DecimalMin("0.00") private BigDecimal discountAmount = BigDecimal.ZERO;
    @DecimalMin("0.00") private BigDecimal shippingFee = BigDecimal.ZERO;
    @DecimalMin("0.00") private BigDecimal otherFee = BigDecimal.ZERO;
    @DecimalMin("0.00") @DecimalMax("100.00") private BigDecimal taxRate = BigDecimal.ZERO;
    @NotEmpty @Valid private List<Item> items;

    @Data
    public static class Item {
        @NotNull private Integer variantId;
        @NotNull @Min(1) private Integer quantity;
        @NotNull @DecimalMin("0.01") private BigDecimal unitCost;
    }
}
