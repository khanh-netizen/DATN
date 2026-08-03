package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {
    List<Banner> findByStatusOrderByPositionAsc(Byte status);

    boolean existsByBannerType(String bannerType);

    List<Banner> findByBannerTypeOrderByPositionAsc(String bannerType);
    
    @Override
    @NonNull
    Page<Banner> findAll(@NonNull Pageable pageable);
}
