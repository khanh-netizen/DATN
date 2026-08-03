package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.WishlistResponse;
import com.foxstyle.api.entity.Product;
import com.foxstyle.api.entity.User;
import com.foxstyle.api.entity.Wishlist;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.ProductRepository;
import com.foxstyle.api.repository.UserRepository;
import com.foxstyle.api.repository.WishlistRepository;
import com.foxstyle.api.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public PageResponse<WishlistResponse> getMyWishlist(String username, Pageable pageable) {
        User user = findUserByUsername(username);
        Page<Wishlist> page = wishlistRepository.findByUserUserId(user.getUserId(), pageable);
        return PageResponse.of(page.map(this::convertToResponse));
    }

    @Override
    @Transactional
    public WishlistResponse addToWishlist(String username, Integer productId) {
        User user = findUserByUsername(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + productId));

        if (wishlistRepository.existsByUserUserIdAndProductProductId(user.getUserId(), productId)) {
            throw new BadRequestException("Sản phẩm đã tồn tại trong danh sách yêu thích của bạn");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .addedDate(LocalDateTime.now())
                .build();

        return convertToResponse(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public void removeFromWishlist(String username, Integer productId) {
        User user = findUserByUsername(username);
        Wishlist wishlist = wishlistRepository.findByUserUserIdAndProductProductId(user.getUserId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong danh sách yêu thích của bạn"));

        wishlistRepository.delete(wishlist);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + username));
    }

    private WishlistResponse convertToResponse(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        return WishlistResponse.builder()
                .wishlistId(wishlist.getWishlistId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .imageUrl(product.getImageUrl())
                .addedDate(wishlist.getAddedDate())
                .build();
    }
}
