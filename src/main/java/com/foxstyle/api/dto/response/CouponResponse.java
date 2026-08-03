package com.foxstyle.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Integer couponId;
    private String couponCode;
    private Byte discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Byte status;
    private Integer categoryId;
    private Byte applicableUserType;
    private Byte applicableScope;
    private String applicableProductIds;
}
