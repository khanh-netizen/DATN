package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.CouponRequest;
import com.foxstyle.api.dto.response.CouponResponse;
import com.foxstyle.api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface CouponService {

    PageResponse<CouponResponse> getAllCoupons(Pageable pageable);

    CouponResponse getCouponById(Integer couponId);

    CouponResponse createCoupon(CouponRequest request);

    CouponResponse updateCoupon(Integer couponId, CouponRequest request);

    void deleteCoupon(Integer couponId);

    /** Khách hàng kiểm tra mã hợp lệ và số tiền được giảm cho giá trị đơn hàng. */
    BigDecimal validateAndCalculateDiscount(String couponCode, BigDecimal orderValue, Integer userId);

    /** Đăng ký bản tin email nhận mã giảm giá (giới hạn 1 lần / email). */
    void subscribeNewsletter(String email, String couponCode);

    /** Kiểm tra xem email đã đăng ký nhận bản tin mã giảm giá chưa. */
    boolean isEmailSubscribed(String email);
}
