package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    @Min(value = 0, message = "Trạng thái đơn hàng hợp lệ từ 0 đến 4")
    @Max(value = 4, message = "Trạng thái đơn hàng hợp lệ từ 0 đến 4")
    private Byte status; // 0-Chờ duyệt, 1-Đã duyệt, 2-Đang giao, 3-Đã giao, 4-Đã hủy
}
