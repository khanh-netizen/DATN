package com.foxstyle.api.repository;

import com.foxstyle.api.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductProductIdOrderByDisplayOrderAsc(Integer productId);
}
