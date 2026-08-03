package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.CartItemRequest;
import com.foxstyle.api.dto.response.CartResponse;

public interface CartService {

    /** Lấy giỏ hàng của user hiện tại (tự tạo nếu chưa có). */
    CartResponse getMyCart(String username);

    CartResponse addItem(String username, CartItemRequest request);

    CartResponse updateItemQuantity(String username, Integer cartDetailId, Integer quantity);

    CartResponse removeItem(String username, Integer cartDetailId);

    void clearCart(String username);
}
