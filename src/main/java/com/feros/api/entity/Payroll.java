package com.feros.api.entity;

import com.feros.api.enums.PaymentMode;
import com.feros.api.enums.PayrollStatus;
import com.feros.api.enums.SalaryType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pay_cycle_start_date", nullable = false)
    private LocalDate payCycleStartDate;

    @Column(name = "pay_cycle_end_date", nullable = false)
    private LocalDate payCycleEndDate;

    @Column(name = "total_days")
    private Integer totalDays = 0;

    @Column(name = "present_days")
    private Integer presentDays = 0;

    @Column(name = "absent_days")
    private Integer absentDays = 0;

    @Column(name = "half_days")
    private Integer halfDays = 0;

    @Column(name = "leave_days")
    private Integer leaveDays = 0;

    @Column(name = "overtime_hours")
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type")
    private SalaryType salaryType = SalaryType.DAILY;

    @Column(name = "daily_rate")
    private BigDecimal dailyRate;

    @Column(name = "monthly_salary", precision = 12, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "basic_pay")
    private BigDecimal basicPay = BigDecimal.ZERO;

    @Column(name = "overtime_pay")
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(name = "trip_bonus")
    private BigDecimal tripBonus = BigDecimal.ZERO;

    @Column(name = "vehicle_extra_pay")
    private BigDecimal vehicleExtraPay = BigDecimal.ZERO;

    @Column(name = "gross_pay")
    private BigDecimal grossPay = BigDecimal.ZERO;

    @Column(name = "total_deductions")
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_pay")
    private BigDecimal netPay = BigDecimal.ZERO;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    private PaymentMode paymentMode;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_status")
    private PayrollStatus payrollStatus = PayrollStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_active")
    private Boolean isActive = true;
}