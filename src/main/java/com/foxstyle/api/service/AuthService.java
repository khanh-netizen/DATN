package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.LoginRequest;
import com.foxstyle.api.dto.request.RegisterRequest;
import com.foxstyle.api.dto.response.AuthResponse;
import com.foxstyle.api.dto.response.UserResponse;

import com.foxstyle.api.dto.request.GoogleLoginRequest;

public interface AuthService {

    /** Đăng nhập, trả về JWT token kèm thông tin người dùng. */
    AuthResponse login(LoginRequest request);

    /** Đăng ký tài khoản khách hàng mới (mặc định ROLE_CUSTOMER). */
    UserResponse register(RegisterRequest request);

    /** Lấy thông tin tài khoản đang đăng nhập. */
    UserResponse getCurrentUser(String username);

    /** Khóa tài khoản tạm thời. */
    void deactivateAccount(String username);

    /** Xóa tài khoản vĩnh viễn. */
    void deleteMyAccount(String username);

    /** Đăng ký tài khoản nhân viên mới (ROLE_STAFF) có kích hoạt trạng thái. */
    UserResponse registerStaff(RegisterRequest request);

    /** Đăng nhập hoặc đăng ký tự động bằng Google Token. */
    AuthResponse googleLogin(GoogleLoginRequest request);

    /** Khôi phục / Đặt lại mật khẩu mới thông qua mã OTP xác thực email. */
    void resetPassword(com.foxstyle.api.dto.request.ResetPasswordRequest request);

    /** Tìm kiếm tài khoản dựa trên Email, SĐT hoặc Username để thực hiện khôi phục mật khẩu. */
    com.foxstyle.api.dto.response.AccountSearchResponse findAccountForPasswordReset(String keyword);
}

