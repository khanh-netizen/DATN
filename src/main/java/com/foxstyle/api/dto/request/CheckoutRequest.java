package com.foxstyle.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    private String recipientName;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    private String recipientPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    private String recipientEmail; // Email để nhận hóa đơn đơn hàng

    private String couponCode; // Nullable

    private BigDecimal shippingFee; // Phí vận chuyển tính theo khoảng cách

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // COD, VNPay, Ví MoMo, Chuyển khoản ngân hàng

    @Valid
    @NotEmpty(message = "Giỏ hàng thanh toán không được rỗng")
    private List<CartItemRequest> items;
}
