package com.foxstyle.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponse {
    private Integer wishlistId;
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String imageUrl;
    private LocalDateTime addedDate;
}
