package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email không được để trống!")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống!")
    private String otp;

    @NotBlank(message = "Mật khẩu mới không được để trống!")
    @Pattern(regexp = com.foxstyle.api.util.PasswordPolicy.REGEX, message = com.foxstyle.api.util.PasswordPolicy.MESSAGE)
    private String newPassword;
}
