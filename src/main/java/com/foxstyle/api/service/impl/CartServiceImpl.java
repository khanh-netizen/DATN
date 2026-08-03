package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.CartItemRequest;
import com.foxstyle.api.dto.response.CartItemResponse;
import com.foxstyle.api.dto.response.CartResponse;
import com.foxstyle.api.entity.*;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.*;
import com.foxstyle.api.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse getMyCart(String username) {
        return convertToResponse(getOrCreateCart(username));
    }

    @Override
    @Transactional
    public CartResponse addItem(String username, CartItemRequest request) {
        Cart cart = getOrCreateCart(username);
        ProductVariant variant = findVariantById(request.getVariantId());

        CartDetail detail = cartDetailRepository
                .findByCartCartIdAndVariantVariantId(cart.getCartId(), variant.getVariantId())
                .orElse(null);

        int newQuantity = (detail != null ? detail.getQuantity() : 0) + request.getQuantity();
        validateStock(variant, newQuantity);

        if (detail == null) {
            detail = CartDetail.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(newQuantity)
                    .build();
        } else {
            detail.setQuantity(newQuantity);
        }
        cartDetailRepository.save(detail);

        return convertToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String username, Integer cartDetailId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Số lượng phải lớn hơn 0");
        }
        Cart cart = getOrCreateCart(username);
        CartDetail detail = findOwnedCartDetail(cart, cartDetailId);

        validateStock(detail.getVariant(), quantity);
        detail.setQuantity(quantity);
        cartDetailRepository.save(detail);

        return convertToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String username, Integer cartDetailId) {
        Cart cart = getOrCreateCart(username);
        cartDetailRepository.delete(findOwnedCartDetail(cart, cartDetailId));
        return convertToResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cartDetailRepository.deleteByCartCartId(cart.getCartId());
    }

    // ==================== Private helpers ====================

    private Cart getOrCreateCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + username));

        return cartRepository.findByUserUserId(user.getUserId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private ProductVariant findVariantById(Integer variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm có ID: " + variantId));
    }

    private CartDetail findOwnedCartDetail(Cart cart, Integer cartDetailId) {
        CartDetail detail = cartDetailRepository.findById(cartDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng giỏ hàng có ID: " + cartDetailId));
        if (!detail.getCart().getCartId().equals(cart.getCartId())) {
            throw new BadRequestException("Dòng giỏ hàng này không thuộc về bạn");
        }
        return detail;
    }

    private void validateStock(ProductVariant variant, int requestedQuantity) {
        if (variant.getQuantity() < requestedQuantity) {
            throw new BadRequestException(String.format(
                    "Sản phẩm màu %s size %s chỉ còn %d sản phẩm trong kho",
                    variant.getColor(), variant.getSize(), variant.getQuantity()));
        }
    }

    private CartResponse convertToResponse(Cart cart) {
        List<CartItemResponse> items = cartDetailRepository.findByCartCartId(cart.getCartId())
                .stream()
                .map(this::convertItem)
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(items)
                .totalItems(items.stream()
                        .mapToInt(CartItemResponse::getQuantity)
                        .sum())
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse convertItem(CartDetail detail) {
        ProductVariant variant = detail.getVariant();
        Product product = variant.getProduct();
        BigDecimal unitPrice = variant.getPrice() != null
                ? variant.getPrice()
                : product.getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(detail.getQuantity()));

        return CartItemResponse.builder()
                .cartDetailId(detail.getCartDetailId())
                .variantId(variant.getVariantId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .imageUrl(product.getImageUrl())
                .color(variant.getColor())
                .size(variant.getSize())
                .price(unitPrice)
                .quantity(detail.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }
}
