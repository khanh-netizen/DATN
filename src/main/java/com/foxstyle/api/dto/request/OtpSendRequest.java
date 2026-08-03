package com.foxstyle.api.dto.request;

import lombok.Data;

@Data
public class OtpSendRequest {
    private String email;
    private String phone;
    private String type; // "email" or "phone"
}
