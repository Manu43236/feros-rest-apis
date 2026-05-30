package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lr_trip_expense_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LrTripExpenseItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_expense_id", nullable = false)
    private LrTripExpense tripExpense;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "approved_amount")
    private BigDecimal approvedAmount;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
