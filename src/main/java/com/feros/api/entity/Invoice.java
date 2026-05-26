package com.feros.api.entity;

import com.feros.api.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "cgst_percentage")
    private BigDecimal cgstPercentage = BigDecimal.ZERO;

    @Column(name = "sgst_percentage")
    private BigDecimal sgstPercentage = BigDecimal.ZERO;

    @Column(name = "igst_percentage")
    private BigDecimal igstPercentage = BigDecimal.ZERO;

    @Column(name = "cgst_amount")
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount")
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount")
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "advance_adjusted")
    private BigDecimal advanceAdjusted = BigDecimal.ZERO;

    @Column(name = "credit_note_adjusted")
    private BigDecimal creditNoteAdjusted = BigDecimal.ZERO;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_due")
    private BigDecimal balanceDue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status")
    private InvoiceStatus invoiceStatus = InvoiceStatus.DRAFT;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_active")
    private Boolean isActive = true;
}