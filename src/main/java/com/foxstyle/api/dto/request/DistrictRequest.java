package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DistrictRequest {

    @NotBlank(message = "Tên quận huyện không được để trống")
    @Size(max = 100, message = "Tên quận huyện tối đa 100 ký tự")
    private String districtName;

    @NotBlank(message = "Tên tỉnh thành phố không được để trống")
    @Size(max = 100, message = "Tên tỉnh thành phố tối đa 100 ký tự")
    private String province;

    @Min(value = 0, message = "Trạng thái chỉ nhận 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái chỉ nhận 0 hoặc 1")
    private Byte status;
}
