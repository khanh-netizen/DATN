package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = com.foxstyle.api.util.PasswordPolicy.REGEX, message = com.foxstyle.api.util.PasswordPolicy.MESSAGE)
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Pattern(regexp = "^$|^(0|\\+84)\\d{8,14}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Mã xác thực OTP không được để trống")
    private String otp;
}
