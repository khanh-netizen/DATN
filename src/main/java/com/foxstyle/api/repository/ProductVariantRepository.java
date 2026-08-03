package com.foxstyle.api.repository;

import com.foxstyle.api.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductProductId(Integer productId);
    Optional<ProductVariant> findByProductProductIdAndColorAndSize(Integer productId, String color, String size);
    boolean existsBySku(String sku);
    boolean existsBySkuAndVariantIdNot(String sku, Integer variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v where v.variantId = :variantId")
    Optional<ProductVariant> findByIdForUpdate(@Param("variantId") Integer variantId);
}
