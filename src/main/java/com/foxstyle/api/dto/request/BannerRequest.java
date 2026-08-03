package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BannerRequest {

    @NotBlank(message = "Tiêu đề banner không được để trống")
    @Size(max = 150, message = "Tiêu đề tối đa 150 ký tự")
    private String title;

    @NotBlank(message = "Ảnh banner không được để trống")
    private String imageUrl;

    @Pattern(regexp = "IMAGE|MARQUEE")
    private String bannerType;

    private String linkUrl;

    @Min(value = 1, message = "Vị trí hiển thị phải >= 1")
    private Integer position;

    @Min(value = 0, message = "Trạng thái chỉ nhận 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái chỉ nhận 0 hoặc 1")
    private Byte status;
}
