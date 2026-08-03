package com.foxstyle.api.repository;

import com.foxstyle.api.entity.PaymentReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentReconciliationRepository
        extends JpaRepository<PaymentReconciliation, Long> {
    Optional<PaymentReconciliation> findByPaymentPaymentId(Integer paymentId);
}
