package com.foxstyle.api.service;

import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.WishlistResponse;
import org.springframework.data.domain.Pageable;

public interface WishlistService {

    PageResponse<WishlistResponse> getMyWishlist(String username, Pageable pageable);

    WishlistResponse addToWishlist(String username, Integer productId);

    void removeFromWishlist(String username, Integer productId);
}
