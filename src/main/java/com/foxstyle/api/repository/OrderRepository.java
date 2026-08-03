package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Order;
import com.foxstyle.api.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUserUserId(Integer userId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByUserUserId(Integer userId);
    long countByUserUserIdAndStatusNot(Integer userId, OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.userId = :userId AND o.status = :status")
    BigDecimal sumTotalAmountByUserAndStatus(@Param("userId") Integer userId, @Param("status") OrderStatus status);

    @Query("""
            SELECT COUNT(od) FROM Order o
            JOIN o.orderDetails od
            WHERE o.user.userId = :userId
              AND o.status = :status
              AND od.variant.product.productId = :productId
            """)
    long countPurchasedProduct(
            @Param("userId") Integer userId,
            @Param("productId") Integer productId,
            @Param("status") OrderStatus status);
}
