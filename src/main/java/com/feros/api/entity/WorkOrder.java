package com.feros.api.entity;

import com.feros.api.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false)
    private RateType rateType;

    @Column(name = "rate_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal rateAmount;

    @Column(name = "shift_hours")
    @Builder.Default
    private Integer shiftHours = 8;

    @Column(name = "overtime_rate_per_hour", precision = 10, scale = 2)
    private BigDecimal overtimeRatePerHour;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_type")
    private OperatorType operatorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_staff_id")
    private StaffProfile operatorStaff;

    @Column(name = "hired_operator_name")
    private String hiredOperatorName;

    @Column(name = "hired_operator_phone")
    private String hiredOperatorPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_billing")
    @Builder.Default
    private OperatorBilling operatorBilling = OperatorBilling.NOT_BILLED;

    @Column(name = "operator_rate_per_day", precision = 10, scale = 2)
    private BigDecimal operatorRatePerDay;

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
}
