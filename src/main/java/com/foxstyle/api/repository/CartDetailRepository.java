package com.foxstyle.api.repository;

import com.foxstyle.api.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Integer> {
    List<CartDetail> findByCartCartId(Integer cartId);
    Optional<CartDetail> findByCartCartIdAndVariantVariantId(Integer cartId, Integer variantId);
    void deleteByCartCartId(Integer cartId);
    void deleteByVariantVariantId(Integer variantId);
}

