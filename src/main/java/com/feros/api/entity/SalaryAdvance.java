package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_advances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryAdvance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "advance_date", nullable = false)
    private LocalDate advanceDate;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "total_repaid")
    private BigDecimal totalRepaid = BigDecimal.ZERO;

    @Column(name = "balance_amount")
    private BigDecimal balanceAmount;

    @Column(name = "is_fully_repaid")
    private Boolean isFullyRepaid = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive = true;
}