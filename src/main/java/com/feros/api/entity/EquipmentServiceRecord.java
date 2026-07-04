package com.feros.api.entity;

import com.feros.api.enums.EquipmentServiceType;
import com.feros.api.enums.ServicePayerType;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipment_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentServiceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "service_number")
    private String serviceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false)
    private ServiceTriggeredBy triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private EquipmentServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payer_type")
    @Builder.Default
    private ServicePayerType payerType = ServicePayerType.OWN_EXPENSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.OPEN;

    @Column(name = "hmr_at_service", precision = 10, scale = 2)
    private BigDecimal hmrAtService;

    @Column(name = "due_at_hmr", precision = 10, scale = 2)
    private BigDecimal dueAtHmr;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "location")
    private String location;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "insurance_claim_no", length = 100)
    private String insuranceClaimNo;

    @Column(name = "insurance_claim_amt", precision = 10, scale = 2)
    private BigDecimal insuranceClaimAmt;

    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    @Column(name = "certificate_valid_until")
    private LocalDate certificateValidUntil;

    @Column(name = "is_escalated")
    @Builder.Default
    private Boolean isEscalated = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<EquipmentServiceTask> tasks = new ArrayList<>();
}
