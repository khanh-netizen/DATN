package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.ReviewRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.ReviewResponse;
import com.foxstyle.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getAllReviews(Pageable pageable) {
        PageResponse<ReviewResponse> reviews = reviewService.getAllReviews(pageable);
        ApiResponse<PageResponse<ReviewResponse>> response = ApiResponse.<PageResponse<ReviewResponse>>builder()
                .status("success")
                .message("Lấy toàn bộ đánh giá sản phẩm thành công")
                .data(reviews)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByProduct(
            @PathVariable Integer productId,
            Pageable pageable) {
        PageResponse<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);
        ApiResponse<PageResponse<ReviewResponse>> response = ApiResponse.<PageResponse<ReviewResponse>>builder()
                .status("success")
                .message("Lấy danh sách đánh giá thành công")
                .data(reviews)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            Principal principal,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse saved = reviewService.createReview(principal.getName(), request);
        ApiResponse<ReviewResponse> response = ApiResponse.<ReviewResponse>builder()
                .status("success")
                .message("Gửi đánh giá thành công")
                .data(saved)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            Principal principal,
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse updated = reviewService.updateReview(principal.getName(), reviewId, request);
        ApiResponse<ReviewResponse> response = ApiResponse.<ReviewResponse>builder()
                .status("success")
                .message("Cập nhật đánh giá thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            Principal principal,
            Authentication authentication,
            @PathVariable Integer reviewId) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(principal.getName(), isAdmin, reviewId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa đánh giá thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
