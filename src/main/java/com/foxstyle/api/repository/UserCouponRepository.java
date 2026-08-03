package com.foxstyle.api.repository;

import com.foxstyle.api.entity.UserCoupon;
import com.foxstyle.api.entity.UserCouponId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, UserCouponId> {
    boolean existsByIdUserIdAndIdCouponId(Integer userId, Integer couponId);
}
