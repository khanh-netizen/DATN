package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "flash_sale_start_at")
    private LocalDateTime flashSaleStartAt;

    @Column(name = "flash_sale_end_at")
    private LocalDateTime flashSaleEndAt;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "origin", length = 100)
    private String origin;

    @Column(name = "care_instructions", columnDefinition = "NVARCHAR(MAX)")
    private String careInstructions;

    @Column(name = "fit_guide", columnDefinition = "NVARCHAR(MAX)")
    private String fitGuide;

    @Column(name = "is_combo", nullable = false, columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private Boolean isCombo = false;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Byte status = 1; // 0 - Ngừng bán, 1 - Đang bán

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images;
}
