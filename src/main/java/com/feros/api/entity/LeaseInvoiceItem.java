package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lease_invoice_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaseInvoiceItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private LeaseInvoice invoice;

    @Column(name = "lease_vehicle_assignment_id")
    private Long leaseVehicleAssignmentId;

    // denormalized for display
    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "days")
    private Integer days;

    @Column(name = "rate", precision = 12, scale = 2)
    private BigDecimal rate;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
