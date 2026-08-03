package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Integer bannerId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "banner_type", length = 20)
    @Builder.Default
    private String bannerType = "IMAGE";

    @Column(name = "link_url", length = 255)
    private String linkUrl;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 1;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Byte status = 1; // 0 - Ẩn, 1 - Hiện
}
