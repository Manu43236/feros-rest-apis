package com.feros.api.entity;

import com.feros.api.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "wo_number", unique = true)
    private String woNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "site")
    private String site;

    @Column(name = "mobilization_charge", precision = 10, scale = 2)
    private BigDecimal mobilizationCharge;

    @Column(name = "demobilization_charge", precision = 10, scale = 2)
    private BigDecimal demobilizationCharge;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WorkOrderStatus status = WorkOrderStatus.DRAFT;

    @Column(name = "parent_wo_id")
    private Long parentWoId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // KAN-16 commercial T&C
    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays;

    @Column(name = "gst_percent", precision = 5, scale = 2)
    private BigDecimal gstPercent;

    @Column(name = "retention_percent", precision = 5, scale = 2)
    private BigDecimal retentionPercent;

    @Column(name = "tds_percent", precision = 5, scale = 2)
    private BigDecimal tdsPercent;

    @Column(name = "billing_cycle_months")
    private Integer billingCycleMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_by_whom")
    private ProviderSide operatorByWhom;

    @Enumerated(EnumType.STRING)
    @Column(name = "diesel_by_whom")
    private ProviderSide dieselByWhom;

    @Column(name = "working_hours_per_day")
    private Integer workingHoursPerDay;

    @Column(name = "sunday_working")
    private Boolean sundayWorking;

    @Column(name = "overtime_rate_multiplier", precision = 5, scale = 2)
    private BigDecimal overtimeRateMultiplier;

    @Column(name = "escalation_clause", columnDefinition = "TEXT")
    private String escalationClause;

    @Column(name = "penalty_clause", columnDefinition = "TEXT")
    private String penaltyClause;

    // E5 KAN-28 — breakdown SLA threshold; exceeded → penalty flag on service record
    @Column(name = "breakdown_penalty_threshold_hours")
    private Integer breakdownPenaltyThresholdHours;
}
