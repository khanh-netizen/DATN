package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CartItemRequest {

    private Integer variantId;

    private Integer comboProductId;
    private Integer comboVariantId;

    private java.util.List<Integer> componentVariantIds;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
}
