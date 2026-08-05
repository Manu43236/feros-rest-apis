package com.feros.api.entity;

import com.feros.api.util.TimeUtil;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_history_id")
    private SubscriptionHistory subscriptionHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "billing_cycle")
    private String billingCycle;

    @Column(name = "vehicle_count")
    private Integer vehicleCount;

    @Column(name = "price_per_vehicle", precision = 10, scale = 2)
    private BigDecimal pricePerVehicle;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "gst_amount", precision = 10, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "installation_charges", precision = 10, scale = 2)
    private BigDecimal installationCharges;

    @Column(name = "payment_ref")
    private String paymentRef;

    @Column(name = "proforma_number", unique = true)
    private String proformaNumber;

    @Column(name = "invoice_status")
    private String invoiceStatus; // PROFORMA / CONFIRMED

    @Column(name = "gst_type")
    private String gstType; // INTRA_STATE / INTER_STATE

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "additional_charges_json", columnDefinition = "TEXT")
    private String additionalChargesJson; // [{name, amount}] stored as JSON

    @Column(name = "fy_year", length = 4)
    private String fyYear; // e.g. "2526" for FY 2025-26

    @Column(name = "sequence_no")
    private Integer sequenceNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = TimeUtil.nowIst(); }
}
