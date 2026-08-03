package com.foxstyle.api.service;

import com.foxstyle.api.entity.Order;

public interface MailService {
    void sendOtpEmail(String email, String otpCode);
    void sendOrderConfirmationEmail(Order order, String recipientEmail);
    void sendDiscountCouponEmail(String email, String couponCode);
}
