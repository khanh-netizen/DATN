package com.foxstyle.api.repository;

import com.foxstyle.api.entity.ProductComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductComboItemRepository extends JpaRepository<ProductComboItem, Integer> {
    List<ProductComboItem> findByComboProductProductIdOrderByDisplayOrderAsc(Integer comboProductId);
    void deleteByComboProductProductId(Integer comboProductId);
    boolean existsByComponentProductProductId(Integer componentProductId);
}
