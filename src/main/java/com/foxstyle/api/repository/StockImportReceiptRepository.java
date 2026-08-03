package com.foxstyle.api.repository;

import com.foxstyle.api.entity.StockImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockImportReceiptRepository extends JpaRepository<StockImportReceipt, Long> {
    List<StockImportReceipt> findAllByOrderByCreatedAtDesc();
}
