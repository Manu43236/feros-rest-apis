package com.feros.api.entity;

import com.feros.api.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment_payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EquipmentPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private EquipmentInvoice invoice;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    @Column(name = "utr_reference")
    private String utrReference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
