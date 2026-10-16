package com.feros.api.entity;

import com.feros.api.enums.DeductionReason;
import com.feros.api.enums.OrderPaymentType;
import com.feros.api.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An order-level money entry: an advance, a balance payment, or a client deduction.
 * Purely additive to the order flow — reconciles what the client actually paid
 * against the order value. Independent of the invoice/InvoicePayment flow.
 */
@Entity
@Table(name = "order_payments", indexes = {
    @Index(name = "idx_order_payments_order", columnList = "order_id"),
    @Index(name = "idx_order_payments_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderPaymentType type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    private PaymentMode paymentMode;          // ADVANCE/PAYMENT only; null for DEDUCTION

    @Enumerated(EnumType.STRING)
    @Column(name = "deduction_reason")
    private DeductionReason deductionReason;  // DEDUCTION only

    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
