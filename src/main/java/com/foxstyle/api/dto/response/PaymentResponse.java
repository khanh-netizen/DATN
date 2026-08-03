package com.foxstyle.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Integer paymentId;
    private Integer orderId;
    private String paymentMethod;
    private Byte paymentStatus;
    private String transactionId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private Boolean reconciled;
    private String reconciliationCode;
    private LocalDateTime reconciledAt;
    private String reconciledBy;
}
