package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.CouponRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.CouponResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.entity.User;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.UserRepository;
import com.foxstyle.api.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserRepository userRepository;
    private final com.foxstyle.api.service.MailService mailService;

    @PostMapping("/subscribe-newsletter")
    public ResponseEntity<ApiResponse<Boolean>> subscribeNewsletter(
            @RequestParam String email,
            @RequestParam(required = false) String couponCode) {
        couponService.subscribeNewsletter(email, couponCode);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .status("success")
                .message("Đã gửi mã giảm giá tới email thành công")
                .data(true)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-newsletter")
    public ResponseEntity<ApiResponse<Boolean>> checkNewsletterSubscription(@RequestParam String email) {
        boolean isSubscribed = couponService.isEmailSubscribed(email);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .status("success")
                .message("Kiểm tra đăng ký bản tin thành công")
                .data(isSubscribed)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CouponResponse>>> getAllCoupons(Pageable pageable) {
        PageResponse<CouponResponse> coupons = couponService.getAllCoupons(pageable);
        ApiResponse<PageResponse<CouponResponse>> response = ApiResponse.<PageResponse<CouponResponse>>builder()
                .status("success")
                .message("Lấy danh sách mã giảm giá thành công")
                .data(coupons)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Integer id) {
        CouponResponse coupon = couponService.getCouponById(id);
        ApiResponse<CouponResponse> response = ApiResponse.<CouponResponse>builder()
                .status("success")
                .message("Lấy mã giảm giá chi tiết thành công")
                .data(coupon)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse saved = couponService.createCoupon(request);
        ApiResponse<CouponResponse> response = ApiResponse.<CouponResponse>builder()
                .status("success")
                .message("Tạo mã giảm giá mới thành công")
                .data(saved)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Integer id,
            @Valid @RequestBody CouponRequest request) {
        CouponResponse updated = couponService.updateCoupon(id, request);
        ApiResponse<CouponResponse> response = ApiResponse.<CouponResponse>builder()
                .status("success")
                .message("Cập nhật mã giảm giá thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Integer id) {
        couponService.deleteCoupon(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa mã giảm giá thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<BigDecimal>> validateCoupon(
            Principal principal,
            @RequestParam String code,
            @RequestParam BigDecimal orderValue) {
        Integer userId = null;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                userId = user.getUserId();
            }
        }

        BigDecimal discount = couponService.validateAndCalculateDiscount(code, orderValue, userId);
        ApiResponse<BigDecimal> response = ApiResponse.<BigDecimal>builder()
                .status("success")
                .message("Mã giảm giá hợp lệ")
                .data(discount)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
