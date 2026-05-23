package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.entity.Tenant;
import com.feros.api.enums.PayCycle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "tenant_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_cycle")
    private PayCycle payCycle = PayCycle.MONTHLY;

    @Column(name = "overtime_threshold_hours")
    private BigDecimal overtimeThresholdHours = BigDecimal.valueOf(8.00);

    @Column(name = "overtime_rate_multiplier")
    private BigDecimal overtimeRateMultiplier = BigDecimal.valueOf(1.50);

    @Column(name = "max_advance_amount")
    private BigDecimal maxAdvanceAmount = BigDecimal.ZERO;

    @Column(name = "max_advance_deduction_per_cycle")
    private BigDecimal maxAdvanceDeductionPerCycle = BigDecimal.ZERO;

    @Column(name = "is_trip_bonus_enabled")
    private Boolean isTripBonusEnabled = false;

    @Column(name = "trip_bonus_amount")
    private BigDecimal tripBonusAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "attendance_enforced")
    private Boolean attendanceEnforced = false;

    @Builder.Default
    @Column(name = "attendance_deadline_time")
    private LocalTime attendanceDeadlineTime = LocalTime.of(8, 0);

    @Builder.Default
    @Column(name = "require_tyre_approval")
    private Boolean requireTyreApproval = false;

    @Builder.Default
    @Column(name = "require_spare_part_approval")
    private Boolean requireSparePartApproval = false;

    @Builder.Default
    @Column(name = "service_gst_rate", precision = 5, scale = 2)
    private java.math.BigDecimal serviceGstRate = java.math.BigDecimal.valueOf(18.00);

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}