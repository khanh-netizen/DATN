package com.foxstyle.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {
    private Integer bannerId;
    private String title;
    private String imageUrl;
    private String bannerType;
    private String linkUrl;
    private Integer position;
    private Byte status;
}
