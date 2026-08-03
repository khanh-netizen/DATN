package com.foxstyle.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {
    private Integer imageId;
    private String imageUrl;
    private Boolean isPrimary;
    private Integer displayOrder;
}
