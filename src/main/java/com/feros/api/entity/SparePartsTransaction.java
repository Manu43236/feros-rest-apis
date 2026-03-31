package com.feros.api.entity;

import com.feros.api.entity.master.SparePart;
import com.feros.api.enums.StockReferenceType;
import com.feros.api.enums.StockTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "spare_parts_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private StockTransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private StockReferenceType referenceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_part_id")
    private ServicePart servicePart;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
