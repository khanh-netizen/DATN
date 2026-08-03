package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Page<Review> findByProductProductId(Integer productId, Pageable pageable);

    Page<Review> findByUserUserId(Integer userId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId AND r.rating > 0")
    Double findAverageRatingByProductId(@Param("productId") Integer productId);
}
