package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.LoginRequest;
import com.foxstyle.api.dto.request.RegisterRequest;
import com.foxstyle.api.dto.request.OtpSendRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.AuthResponse;
import com.foxstyle.api.dto.response.UserResponse;
import com.foxstyle.api.service.AuthService;
import com.foxstyle.api.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final com.foxstyle.api.config.DatabaseEncodingRepair databaseEncodingRepair;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Đăng nhập thành công")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .status("success")
                .message("Đăng ký tài khoản thành công")
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register-staff")
    public ResponseEntity<ApiResponse<UserResponse>> registerStaff(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.registerStaff(request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .status("success")
                .message("Đăng ký tài khoản nhân viên thành công")
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse userResponse = authService.getCurrentUser(principal.getName());
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .status("success")
                .message("Lấy thông tin tài khoản thành công")
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.deactivateAccount(principal.getName());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Khóa tài khoản thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.deleteMyAccount(principal.getName());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa tài khoản thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody com.foxstyle.api.dto.request.GoogleLoginRequest request) {
        AuthResponse authResponse = authService.googleLogin(request);
        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Đăng nhập bằng Google thành công")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        String otpCode = otpService.sendOtp(request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("success")
                .message("Gửi mã OTP thành công")
                .data(otpCode)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestBody com.foxstyle.api.dto.request.OtpVerifyRequest request) {
        String target = "email".equalsIgnoreCase(request.getType()) ? request.getEmail() : request.getPhone();
        if (target == null || target.trim().isEmpty()) {
            throw new com.foxstyle.api.exception.BadRequestException("Thông tin nhận mã OTP không được để trống!");
        }
        otpService.verifyOtp(target.trim(), request.getOtp());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xác thực mã OTP thành công!")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/firebase-success")
    public ResponseEntity<ApiResponse<Void>> firebaseSuccess(@RequestBody com.foxstyle.api.dto.request.OtpVerifyRequest request) {
        String target = request.getPhone();
        if (target == null || target.trim().isEmpty()) {
            throw new com.foxstyle.api.exception.BadRequestException("Số điện thoại không được để trống!");
        }
        otpService.createVerifiedOtpForFirebase(target.trim(), request.getOtp());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Đồng bộ OTP Firebase thành công!")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/forgot-password/find-account")
    public ResponseEntity<ApiResponse<com.foxstyle.api.dto.response.AccountSearchResponse>> findAccountForPasswordReset(
            @RequestParam String keyword) {
        com.foxstyle.api.dto.response.AccountSearchResponse account = authService.findAccountForPasswordReset(keyword);
        ApiResponse<com.foxstyle.api.dto.response.AccountSearchResponse> response = ApiResponse.<com.foxstyle.api.dto.response.AccountSearchResponse>builder()
                .status("success")
                .message("Đã tìm thấy tài khoản tương ứng trên hệ thống!")
                .data(account)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<String>> sendForgotPasswordOtp(@Valid @RequestBody com.foxstyle.api.dto.request.ForgotPasswordSendOtpRequest request) {

        String otpCode = otpService.sendForgotPasswordOtp(request.getEmail());
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("success")
                .message("Mã OTP khôi phục mật khẩu đã được gửi đến email của bạn")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody com.foxstyle.api.dto.request.ResetPasswordRequest request) {
        authService.resetPassword(request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/repair-db")
    public ResponseEntity<ApiResponse<String>> repairDb() {
        databaseEncodingRepair.repairDatabase();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("success")
                .message("Đã thực hiện sửa lỗi font chữ cơ sở dữ liệu thành công!")
                .data("OK")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
