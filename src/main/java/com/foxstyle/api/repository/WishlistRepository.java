package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    Page<Wishlist> findByUserUserId(Integer userId, Pageable pageable);
    Optional<Wishlist> findByUserUserIdAndProductProductId(Integer userId, Integer productId);
    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);
}
