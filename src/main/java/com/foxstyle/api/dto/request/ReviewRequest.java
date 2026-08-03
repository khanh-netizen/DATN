package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Sản phẩm đánh giá không được để trống")
    private Integer productId;

    @NotNull(message = "Số sao đánh giá không được để trống")
    @Min(value = 0, message = "Đánh giá từ 0 đến 5 sao")
    @Max(value = 5, message = "Đánh giá từ 1 đến 5 sao")
    private Byte rating;

    private String comment;
}
