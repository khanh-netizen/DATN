package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByOrderOrderId(Integer orderId);
    List<Payment> findByOrderOrderIdOrderByPaymentIdDesc(Integer orderId);
    Optional<Payment> findTopByOrderOrderIdOrderByPaymentIdDesc(Integer orderId);
    Page<Payment> findByPaymentStatus(Byte paymentStatus, Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.paymentId = (
                SELECT MAX(p2.paymentId)
                FROM Payment p2
                WHERE p2.order.orderId = p.order.orderId
            )
            AND (:status IS NULL OR p.paymentStatus = :status)
            """)
    Page<Payment> findLatestPerOrder(@Param("status") Byte status, Pageable pageable);
}
