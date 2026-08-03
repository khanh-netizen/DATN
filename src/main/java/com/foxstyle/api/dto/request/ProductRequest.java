package com.foxstyle.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class ProductRequest {

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private Integer categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    private String productName;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", message = "Giá bán phải >= 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Giá gốc phải >= 0")
    private BigDecimal originalPrice;
    private LocalDateTime flashSaleStartAt;
    private LocalDateTime flashSaleEndAt;

    private String description;

    private String imageUrl;

    private String material;

    @Size(max = 100, message = "Thương hiệu tối đa 100 ký tự")
    private String brand;

    private String origin;

    private String careInstructions;

    private String fitGuide;

    private Boolean isCombo;

    private List<Integer> comboProductIds;
    private List<Integer> comboGiftProductIds;

    private String videoUrl;

    @Min(value = 0, message = "Trạng thái chỉ nhận 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái chỉ nhận 0 hoặc 1")
    private Byte status;

    @Valid
    private List<ProductVariantRequest> variants;

    @Valid
    private List<ProductImageRequest> images;
}
