package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetStaffPasswordRequest {
    @NotBlank(message = "Căn cước công dân không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "Căn cước công dân phải gồm đúng 12 chữ số")
    private String citizenId;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(regexp = com.foxstyle.api.util.PasswordPolicy.REGEX, message = com.foxstyle.api.util.PasswordPolicy.MESSAGE)
    private String newPassword;
}
