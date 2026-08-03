package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCouponCode(String couponCode);
    boolean existsByCouponCode(String couponCode);
    Page<Coupon> findByStatus(Byte status, Pageable pageable);
}
