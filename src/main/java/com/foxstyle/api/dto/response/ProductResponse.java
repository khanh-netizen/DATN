package com.foxstyle.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Integer productId;
    private Integer categoryId;
    private String categoryName;
    private String productName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private LocalDateTime flashSaleStartAt;
    private LocalDateTime flashSaleEndAt;
    private String description;
    private String imageUrl;
    private String material;
    private String brand;
    private String origin;
    private String careInstructions;
    private String fitGuide;
    private Boolean isCombo;
    private List<Integer> comboProductIds;
    private List<Integer> comboGiftProductIds;
    private Byte status;
    private Double averageRating;
    private String videoUrl;
    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;
}
