package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Page<Product> findByStatus(Byte status, Pageable pageable);

    boolean existsByProductNameIgnoreCase(String productName);

    Page<Product> findByCategoryCategoryIdAndStatus(Integer categoryId, Byte status, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCaseAndStatus(String keyword, Byte status, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE (:categoryId IS NULL OR p.category.categoryId = :categoryId)
              AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<Product> filterProducts(@Param("categoryId") Integer categoryId,
                                 @Param("keyword") String keyword,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);
}
