package com.foxstyle.api.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    private Integer orderDetailId;
    private Integer variantId;
    private Integer productId;
    private String productName;
    private String imageUrl;
    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal lineTotal;
}
