package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "districts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "district_name", nullable = false, length = 100)
    private String districtName;

    @Column(name = "province", nullable = false, length = 100)
    private String province;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Byte status = 1; // 0 - Inactive, 1 - Active
}
