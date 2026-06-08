package com.feros.api.entity;

import com.feros.api.enums.ServiceInvoiceStatus;
import com.feros.api.enums.ServiceInvoiceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false, unique = true)
    private VehicleService service;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false)
    private ServiceInvoiceType invoiceType;

    // ── INTERNAL fields ──────────────────────────────────────────────────────
    @Column(name = "tasks_total", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tasksTotal = BigDecimal.ZERO;

    @Column(name = "parts_total", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal partsTotal = BigDecimal.ZERO;

    @Column(name = "labour_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal labourCharges = BigDecimal.ZERO;

    @Column(name = "sub_total", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(name = "gst_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstRate = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // ── EXTERNAL fields ──────────────────────────────────────────────────────
    @Column(name = "vendor_amount", precision = 10, scale = 2)
    private BigDecimal vendorAmount;

    @Column(name = "vendor_invoice_no", length = 100)
    private String vendorInvoiceNo;

    // ── Payment ───────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private ServiceInvoiceStatus paymentStatus = ServiceInvoiceStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_id")
    private User paidBy;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
