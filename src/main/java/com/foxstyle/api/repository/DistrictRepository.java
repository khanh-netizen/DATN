package com.foxstyle.api.repository;

import com.foxstyle.api.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    Page<District> findByStatus(Byte status, Pageable pageable);
    boolean existsByDistrictNameIgnoreCaseAndProvinceIgnoreCase(String districtName, String province);
}
