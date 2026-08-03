package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {

    @NotBlank(message = "Đường dẫn ảnh không được để trống")
    private String imageUrl;

    private Boolean isPrimary;

    private Integer displayOrder;
}
