package com.feros.api.entity;

import com.feros.api.enums.AttachmentType;
import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.HireRateUnit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AttachmentType type;

    @Column(name = "serial_number")
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type", nullable = false)
    private EquipmentOwnershipType ownershipType;

    // Sub-hire details (when ownershipType is hired-in), mirrors Equipment
    @Column(name = "hired_from")
    private String hiredFrom;

    @Column(name = "hire_start_date")
    private LocalDate hireStartDate;

    @Column(name = "hire_end_date")
    private LocalDate hireEndDate;

    // Own optional rate — stored here, consumed by the billing engine (E7)
    @Column(name = "default_rate", precision = 12, scale = 2)
    private BigDecimal defaultRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_unit")
    private HireRateUnit rateUnit;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
