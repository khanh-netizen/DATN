package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Integer couponId;

    @Column(name = "coupon_code", nullable = false, unique = true, length = 50)
    private String couponCode;

    @Column(name = "discount_type", nullable = false)
    private Byte discountType; // 1 - Tiền cố định (VNĐ), 2 - Phần trăm (%)

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_value", precision = 12, scale = 2)
    private BigDecimal maxDiscountValue;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "usage_limit", nullable = false)
    @Builder.Default
    private Integer usageLimit = 100;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Byte status = 1; // 0 - Vô hiệu hóa, 1 - Kích hoạt

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "applicable_user_type", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    @Builder.Default
    private Byte applicableUserType = 0; // 0 - Tất cả, 1 - Thành viên mới, 2 - Thành viên cũ

    @Column(name = "applicable_scope", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    @Builder.Default
    private Byte applicableScope = 0; // 0 - Tất cả sản phẩm, 1 - Theo danh mục, 2 - Sản phẩm chọn lọc

    @Column(name = "applicable_product_ids", length = 1000)
    private String applicableProductIds;
}
