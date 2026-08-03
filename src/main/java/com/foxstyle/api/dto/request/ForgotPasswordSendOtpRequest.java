package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordSendOtpRequest {
    @NotBlank(message = "Email không được để trống!")
    private String email;
}
