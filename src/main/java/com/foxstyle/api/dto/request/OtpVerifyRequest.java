package com.foxstyle.api.dto.request;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String email;
    private String phone;
    private String type; // "email" or "phone"
    private String otp;
}
