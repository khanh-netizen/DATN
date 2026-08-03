package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Dùng cho ADMIN tạo/cập nhật tài khoản (có thể gán role bất kỳ).
 */
@Data
public class UserRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    // Cho phép null khi cập nhật (giữ mật khẩu cũ)
    @Pattern(regexp = com.foxstyle.api.util.PasswordPolicy.REGEX, message = com.foxstyle.api.util.PasswordPolicy.MESSAGE)
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;

    @Pattern(regexp = "^$|^[0-9]{12}$", message = "Căn cước công dân phải gồm đúng 12 chữ số")
    private String citizenId;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    @NotNull(message = "Role không được để trống")
    private Integer roleId;

    @Min(value = 0, message = "Trạng thái chỉ nhận 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái chỉ nhận 0 hoặc 1")
    private Byte status;
}
