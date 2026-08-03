package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Page<Category> findByStatus(Byte status, Pageable pageable);
    boolean existsByCategoryNameIgnoreCase(String categoryName);
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
}

