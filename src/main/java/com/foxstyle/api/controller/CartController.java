package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.CartItemRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.CartResponse;
import com.foxstyle.api.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/my-cart")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(Principal principal) {
        CartResponse cart = cartService.getMyCart(principal.getName());
        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .status("success")
                .message("Lấy giỏ hàng thành công")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            Principal principal,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse cart = cartService.addItem(principal.getName(), request);
        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .status("success")
                .message("Thêm sản phẩm vào giỏ hàng thành công")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{detailId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            Principal principal,
            @PathVariable Integer detailId,
            @RequestParam Integer quantity) {
        CartResponse cart = cartService.updateItemQuantity(principal.getName(), detailId, quantity);
        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .status("success")
                .message("Cập nhật số lượng sản phẩm thành công")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{detailId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            Principal principal,
            @PathVariable Integer detailId) {
        CartResponse cart = cartService.removeItem(principal.getName(), detailId);
        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .status("success")
                .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa sạch giỏ hàng thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
