package com.foxstyle.api.controller;

import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.WishlistResponse;
import com.foxstyle.api.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WishlistResponse>>> getMyWishlist(
            Principal principal,
            Pageable pageable) {
        PageResponse<WishlistResponse> wishlist = wishlistService.getMyWishlist(principal.getName(), pageable);
        ApiResponse<PageResponse<WishlistResponse>> response = ApiResponse.<PageResponse<WishlistResponse>>builder()
                .status("success")
                .message("Lấy danh sách sản phẩm yêu thích thành công")
                .data(wishlist)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            Principal principal,
            @PathVariable Integer productId) {
        WishlistResponse saved = wishlistService.addToWishlist(principal.getName(), productId);
        ApiResponse<WishlistResponse> response = ApiResponse.<WishlistResponse>builder()
                .status("success")
                .message("Đã thêm sản phẩm vào danh sách yêu thích")
                .data(saved)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            Principal principal,
            @PathVariable Integer productId) {
        wishlistService.removeFromWishlist(principal.getName(), productId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Đã xóa sản phẩm khỏi danh sách yêu thích")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
