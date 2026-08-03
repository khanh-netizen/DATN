package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reconciliations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reconciliation_id")
    private Long reconciliationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "reconciliation_code", nullable = false, unique = true, length = 100)
    private String reconciliationCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reconciled_by")
    private User reconciledBy;

    @Column(name = "reconciled_at", nullable = false)
    @Builder.Default
    private LocalDateTime reconciledAt = LocalDateTime.now();

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "reconciled";

    @Column(name = "note", length = 500)
    private String note;
}
