package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 50, message = "Mã giảm giá tối đa 50 ký tự")
    private String couponCode;

    @NotNull(message = "Loại giảm giá không được để trống")
    @Min(value = 1, message = "Loại giảm giá: 1 - Tiền cố định, 2 - Phần trăm")
    @Max(value = 2, message = "Loại giảm giá: 1 - Tiền cố định, 2 - Phần trăm")
    private Byte discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Giá trị đơn tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0.0", message = "Giảm tối đa phải >= 0")
    private BigDecimal maxDiscountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @Min(value = 1, message = "Số lượng phát hành phải >= 1")
    private Integer usageLimit;

    @Min(value = 0, message = "Trạng thái chỉ nhận 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái chỉ nhận 0 hoặc 1")
    private Byte status;

    private Integer categoryId;

    private Byte applicableUserType; // 0 - Tất cả, 1 - Thành viên mới, 2 - Thành viên cũ

    private Byte applicableScope; // 0 - Tất cả sản phẩm, 1 - Theo danh mục, 2 - Sản phẩm chọn lọc

    private String applicableProductIds;
}
