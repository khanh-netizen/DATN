package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductVariantRequest {

    // Null khi tạo mới, có giá trị khi cập nhật biến thể sẵn có
    private Integer variantId;

    @NotBlank(message = "Màu sắc không được để trống")
    private String color;

    @NotBlank(message = "Kích thước không được để trống")
    private String size;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải >= 0")
    private Integer quantity;

    private String sku;

    @NotNull(message = "Giá biến thể không được để trống")
    @DecimalMin(value = "0.01", message = "Giá biến thể phải lớn hơn 0")
    private java.math.BigDecimal price;
    private java.math.BigDecimal costPrice;

    private String imageUrl;
}
