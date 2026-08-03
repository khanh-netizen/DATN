package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SettingRequest {

    @NotBlank(message = "Key cấu hình không được để trống")
    @Size(max = 100, message = "Key cấu hình tối đa 100 ký tự")
    private String settingKey;

    private String settingValue;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;
}
