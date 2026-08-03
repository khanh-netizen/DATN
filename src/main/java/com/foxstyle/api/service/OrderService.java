package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.CheckoutRequest;
import com.foxstyle.api.dto.response.OrderResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.entity.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /** ADMIN/STAFF: toàn bộ đơn hàng, lọc theo trạng thái nếu có. */
    PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable);

    /** Khách hàng: lịch sử đơn hàng của chính mình. */
    PageResponse<OrderResponse> getMyOrders(String username, Pageable pageable);

    /** Khách chỉ được xem đơn của mình; ADMIN/STAFF xem tất cả. */
    OrderResponse getOrderById(Integer orderId, String username, boolean isStaff);

    /** Khách đặt hàng: trừ kho, áp coupon, tạo bản ghi thanh toán. */
    OrderResponse checkout(String username, CheckoutRequest request);

    OrderResponse updateOrderStatus(
            Integer orderId,
            OrderStatus status,
            String reason,
            Boolean warrantyRedelivery);

    /** Khách tự hủy đơn khi còn ở trạng thái chờ duyệt; hoàn lại tồn kho. */
    OrderResponse cancelMyOrder(String username, Integer orderId, String reason);

    OrderResponse requestReturn(
            String username,
            Integer orderId,
            String reason,
            Boolean warrantyRedelivery);

    OrderResponse dispatchToCarrier(Integer orderId, String carrier);

    OrderResponse updateShippingFee(Integer orderId, java.math.BigDecimal shippingFee);
}
