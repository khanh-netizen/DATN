package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.CouponRequest;
import com.foxstyle.api.dto.response.CouponResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.entity.Coupon;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.CouponRepository;
import com.foxstyle.api.repository.UserCouponRepository;
import com.foxstyle.api.repository.CartRepository;
import com.foxstyle.api.entity.Cart;
import com.foxstyle.api.service.CouponService;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.foxstyle.api.entity.NewsletterSubscription;
import com.foxstyle.api.repository.NewsletterSubscriptionRepository;
import com.foxstyle.api.service.MailService;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CouponServiceImpl implements CouponService {

    private static final byte DISCOUNT_TYPE_FIXED = 1;
    private static final byte DISCOUNT_TYPE_PERCENT = 2;

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CartRepository cartRepository;
    private final com.foxstyle.api.repository.OrderRepository orderRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final MailService mailService;

    @Override
    public PageResponse<CouponResponse> getAllCoupons(Pageable pageable) {
        return PageResponse.of(couponRepository.findAll(pageable).map(this::convertToResponse));
    }

    @Override
    public CouponResponse getCouponById(Integer couponId) {
        return convertToResponse(findCouponById(couponId));
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCouponCode(request.getCouponCode())) {
            throw new BadRequestException("Mã giảm giá đã tồn tại: " + request.getCouponCode());
        }
        validateDates(request);
        validateDiscountValue(request);

        Coupon coupon = Coupon.builder()
                .couponCode(request.getCouponCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO)
                .maxDiscountValue(request.getMaxDiscountValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 100)
                .usedCount(0)
                .status(request.getStatus() != null ? request.getStatus() : (byte) 1)
                .categoryId(request.getCategoryId())
                .applicableUserType(request.getApplicableUserType() != null ? request.getApplicableUserType() : 0)
                .applicableScope(request.getApplicableScope() != null ? request.getApplicableScope() : 0)
                .applicableProductIds(request.getApplicableProductIds())
                .build();

        return convertToResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Integer couponId, CouponRequest request) {
        Coupon coupon = findCouponById(couponId);
        validateDates(request);
        validateDiscountValue(request);

        boolean codeChanged = !coupon.getCouponCode().equalsIgnoreCase(request.getCouponCode());
        if (codeChanged && couponRepository.existsByCouponCode(request.getCouponCode().toUpperCase())) {
            throw new BadRequestException("Mã giảm giá đã tồn tại: " + request.getCouponCode());
        }

        coupon.setCouponCode(request.getCouponCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        if (request.getMinOrderValue() != null) {
            coupon.setMinOrderValue(request.getMinOrderValue());
        }
        coupon.setMaxDiscountValue(request.getMaxDiscountValue());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        if (request.getUsageLimit() != null) {
            coupon.setUsageLimit(request.getUsageLimit());
        }
        if (request.getStatus() != null) {
            coupon.setStatus(request.getStatus());
        }
        coupon.setCategoryId(request.getCategoryId());
        if (request.getApplicableUserType() != null) {
            coupon.setApplicableUserType(request.getApplicableUserType());
        }
        if (request.getApplicableScope() != null) {
            coupon.setApplicableScope(request.getApplicableScope());
        }
        coupon.setApplicableProductIds(request.getApplicableProductIds());

        return convertToResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public void deleteCoupon(Integer couponId) {
        Coupon coupon = findCouponById(couponId);
        // Vô hiệu hóa thay vì xóa cứng để bảo toàn lịch sử đơn hàng
        coupon.setStatus((byte) 0);
        couponRepository.save(coupon);
    }

    @Override
    public BigDecimal validateAndCalculateDiscount(String couponCode, BigDecimal orderValue, Integer userId) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập mã giảm giá!");
        }
        Coupon coupon = couponRepository.findByCouponCode(couponCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại: " + couponCode));

        validateCouponUsable(coupon, orderValue, userId);
        return calculateDiscount(coupon, orderValue);
    }

    // ==================== Private helpers ====================

    private Coupon findCouponById(Integer couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá có ID: " + couponId));
    }

    private void validateDates(CouponRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private void validateDiscountValue(CouponRequest request) {
        if (request.getDiscountType() == DISCOUNT_TYPE_PERCENT
                && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) >= 0) {
            throw new BadRequestException("Mã giảm giá theo phần trăm phải nhỏ hơn 100%");
        }
    }

    private void validateCouponUsable(Coupon coupon, BigDecimal orderValue, Integer userId) {
        LocalDateTime now = LocalDateTime.now();

        if (coupon.getStatus() != 1) {
            throw new BadRequestException("Mã giảm giá đã bị vô hiệu hóa");
        }
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            throw new BadRequestException("Mã giảm giá không nằm trong thời gian áp dụng");
        }
        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng");
        }
        if (orderValue.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BadRequestException(String.format(
                    "Đơn hàng tối thiểu %,.0fđ mới được áp dụng mã %s",
                    coupon.getMinOrderValue(), coupon.getCouponCode()));
        }

        // Quy tắc 1: Mỗi mã chỉ dùng 1 lần / 1 tài khoản
        if (userId != null && userCouponRepository.existsByIdUserIdAndIdCouponId(userId, coupon.getCouponId())) {
            throw new BadRequestException("Bạn đã sử dụng mã giảm giá này rồi trên tài khoản này.");
        }

        // Quy tắc 2: Phân loại Thành viên Mới vs Thành viên Cũ
        if (userId != null && coupon.getApplicableUserType() != null && coupon.getApplicableUserType() > 0) {
            long userOrderCount = orderRepository.countByUserUserId(userId);
            if (coupon.getApplicableUserType() == 1 && userOrderCount > 0) { // NEW_MEMBER
                throw new BadRequestException("Mã giảm giá này chỉ dành riêng cho Thành viên mới chưa từng mua hàng.");
            }
            if (coupon.getApplicableUserType() == 2 && userOrderCount == 0) { // EXISTING_MEMBER
                throw new BadRequestException("Mã giảm giá này chỉ dành riêng cho Thành viên cũ đã từng mua hàng.");
            }
        }

        // Quy tắc 3: Phạm vi áp dụng sản phẩm (Tất cả / Danh mục / Sản phẩm chọn lọc)
        if (userId != null && coupon.getApplicableScope() != null && coupon.getApplicableScope() > 0) {
            Optional<Cart> userCartOpt = cartRepository.findByUserUserId(userId);
            if (userCartOpt.isPresent()) {
                Cart cart = userCartOpt.get();
                if (coupon.getApplicableScope() == 1 && coupon.getCategoryId() != null) { // CATEGORY
                    boolean hasCategoryProduct = cart.getCartDetails().stream()
                            .anyMatch(detail -> detail.getVariant().getProduct().getCategory().getCategoryId().equals(coupon.getCategoryId()));
                    if (!hasCategoryProduct) {
                        throw new BadRequestException("Mã giảm giá này chỉ áp dụng cho sản phẩm thuộc Danh mục được quy định.");
                    }
                } else if (coupon.getApplicableScope() == 2 && coupon.getApplicableProductIds() != null && !coupon.getApplicableProductIds().trim().isEmpty()) { // SPECIFIC_PRODUCTS
                    java.util.List<String> targetProdIds = java.util.Arrays.asList(coupon.getApplicableProductIds().split(","));
                    boolean hasSpecificProduct = cart.getCartDetails().stream()
                            .anyMatch(detail -> targetProdIds.contains(String.valueOf(detail.getVariant().getProduct().getProductId())));
                    if (!hasSpecificProduct) {
                        throw new BadRequestException("Mã giảm giá này chỉ áp dụng cho các Sản phẩm chỉ định được chọn.");
                    }
                }
            }
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderValue) {
        BigDecimal discount;
        if (coupon.getDiscountType() == DISCOUNT_TYPE_FIXED) {
            discount = coupon.getDiscountValue();
        } else if (coupon.getDiscountType() == DISCOUNT_TYPE_PERCENT) {
            discount = orderValue.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountValue() != null && discount.compareTo(coupon.getMaxDiscountValue()) > 0) {
                discount = coupon.getMaxDiscountValue();
            }
        } else {
            throw new BadRequestException("Loại giảm giá không hợp lệ");
        }
        return discount.min(orderValue);
    }

    private CouponResponse convertToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getCouponId())
                .couponCode(coupon.getCouponCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscountValue(coupon.getMaxDiscountValue())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .status(coupon.getStatus())
                .categoryId(coupon.getCategoryId())
                .applicableUserType(coupon.getApplicableUserType())
                .applicableScope(coupon.getApplicableScope())
                .applicableProductIds(coupon.getApplicableProductIds())
                .build();
    }

    @Override
    @Transactional
    public void subscribeNewsletter(String email, String couponCode) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Địa chỉ email không được để trống!");
        }
        String cleanEmail = email.trim().toLowerCase();

        if (newsletterSubscriptionRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("Email/Tài khoản này đã nhận mã giảm giá trước đó. Mỗi tài khoản chỉ được nhận 1 lần duy nhất!");
        }

        NewsletterSubscription subscription = NewsletterSubscription.builder()
                .email(cleanEmail)
                .subscribedAt(LocalDateTime.now())
                .build();
        newsletterSubscriptionRepository.save(subscription);

        String code = (couponCode != null && !couponCode.trim().isEmpty()) ? couponCode.trim() : "FOXSTYLE50";
        mailService.sendDiscountCouponEmail(cleanEmail, code);
    }

    @Override
    public boolean isEmailSubscribed(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return newsletterSubscriptionRepository.existsByEmail(email.trim().toLowerCase());
    }
}

