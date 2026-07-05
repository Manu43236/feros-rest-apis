package com.feros.api.entity;

import com.feros.api.enums.EquipmentBillingType;
import com.feros.api.enums.EquipmentInvoiceItemType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "equipment_invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private EquipmentInvoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private EquipmentInvoiceItemType itemType;

    @Column(name = "description", nullable = false)
    private String description;

    // For MACHINE items — optional reference + denormalized display fields
    @Column(name = "machine_assignment_id")
    private Long machineAssignmentId;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "equipment_type_name")
    private String equipmentTypeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type")
    private EquipmentBillingType billingType;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "rate", precision = 12, scale = 2)
    private BigDecimal rate;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
