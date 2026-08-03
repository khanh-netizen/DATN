package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.OtpSendRequest;

public interface OtpService {
    /**
     * Tạo mã OTP 6 số, lưu vào cơ sở dữ liệu và gửi đến Email hoặc Số điện thoại của người dùng.
     */
    String sendOtp(OtpSendRequest request);

    /**
     * Xác thực mã OTP nhận được từ người dùng.
     * Ném ngoại lệ BadRequestException nếu mã không hợp lệ hoặc đã hết hạn.
     */
    void verifyOtp(String email, String otpCode);

    /**
     * Tạo mã OTP 6 số, lưu vào cơ sở dữ liệu và gửi đến Email người dùng để khôi phục mật khẩu.
     */
    String sendForgotPasswordOtp(String email);

    /**
     * Tạo bản ghi OTP đã xác thực bằng Firebase để đồng bộ với cơ sở dữ liệu khi đăng ký.
     */
    void createVerifiedOtpForFirebase(String phone, String otp);
}
