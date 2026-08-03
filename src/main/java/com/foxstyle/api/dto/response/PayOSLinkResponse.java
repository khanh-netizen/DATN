package com.foxstyle.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSLinkResponse {
    private Integer orderId;
    private Long orderCode;
    private String checkoutUrl;
    private String qrCode;
    private BigDecimal amount;
    private String status; // PENDING, PAID, CANCELLED
    private String paymentLinkId;
    private String accountName;
    private String accountNumber;
    private String bin;
    private String description;
}
