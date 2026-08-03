package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.OtpSendRequest;
import com.foxstyle.api.entity.OtpVerification;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.repository.OtpVerificationRepository;
import com.foxstyle.api.repository.UserRepository;
import com.foxstyle.api.service.OtpService;
import lombok.RequiredArgsConstructor;
import com.foxstyle.api.service.MailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    private static final int OTP_EXPIRY_MINUTES = 3;

    @Override
    @Transactional
    public String sendOtp(OtpSendRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email không được để trống!");
        }
        String target = request.getEmail().trim();
        if (userRepository.existsByEmail(target)) {
            throw new BadRequestException("Email này đã được sử dụng!");
        }

        // Tạo mã OTP ngẫu nhiên 6 số
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(code);

        // Xóa các mã OTP cũ của target này trước khi tạo mã mới
        otpRepository.deleteByEmail(target);

        // Lưu OTP vào DB
        OtpVerification otpVerification = OtpVerification.builder()
                .email(target)
                .otpCode(otpCode)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .build();
        
        otpRepository.save(otpVerification);

        // Gửi email
        mailService.sendOtpEmail(target, otpCode);
        System.out.println("[OTP BYPASS] Mã xác thực OTP cho " + target + " là: " + otpCode);

        return otpCode;
    }

    @Override
    @Transactional
    public String sendForgotPasswordOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email không được để trống!");
        }
        String target = email.trim();
        if (!userRepository.existsByEmail(target)) {
            throw new BadRequestException("Email này chưa đăng ký tài khoản trên hệ thống!");
        }

        // Tạo mã OTP ngẫu nhiên 6 số
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(code);

        // Xóa các mã OTP cũ của target này trước khi tạo mã mới
        otpRepository.deleteByEmail(target);

        // Lưu OTP vào DB
        OtpVerification otpVerification = OtpVerification.builder()
                .email(target)
                .otpCode(otpCode)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .build();

        otpRepository.save(otpVerification);

        // Gửi email
        mailService.sendOtpEmail(target, otpCode);
        System.out.println("[FORGOT-PASSWORD OTP] Mã xác thực OTP cho " + target + " là: " + otpCode);

        return otpCode;
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otpCode) {
        OtpVerification otpVerification = otpRepository.findFirstByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy mã OTP được gửi đến địa chỉ này!"));

        if (otpVerification.getVerified()) {
            if (otpVerification.getExpiryTime().isAfter(LocalDateTime.now()) && otpVerification.getOtpCode().equals(otpCode)) {
                return; // Đã xác thực thành công từ trước và vẫn còn hiệu lực, cho phép đi qua
            }
            throw new BadRequestException("Mã OTP này đã được sử dụng!");
        }

        if (otpVerification.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã OTP đã hết hạn! Vui lòng yêu cầu mã mới.");
        }

        if (!otpVerification.getOtpCode().equals(otpCode)) {
            throw new BadRequestException("Mã OTP không chính xác. Vui lòng thử lại!");
        }

        // Đánh dấu đã xác thực thành công
        otpVerification.setVerified(true);
        otpRepository.save(otpVerification);
    }

    @Override
    @Transactional
    public void createVerifiedOtpForFirebase(String phone, String otp) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BadRequestException("Số điện thoại không được để trống!");
        }
        String target = phone.trim();

        // Xóa các mã OTP cũ
        otpRepository.deleteByEmail(target);

        // Lưu bản ghi OTP đã xác nhận
        OtpVerification otpVerification = OtpVerification.builder()
                .email(target)
                .otpCode(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5)) // Hiệu lực 5 phút để hoàn tất đăng ký
                .verified(true)
                .build();

        otpRepository.save(otpVerification);
        System.out.println("[FIREBASE-OTP] Đã tạo và đồng bộ mã OTP xác thực Firebase cho: " + target);
    }
}
