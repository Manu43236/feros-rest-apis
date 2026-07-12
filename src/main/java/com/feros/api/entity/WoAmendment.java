package com.feros.api.entity;

import com.feros.api.enums.AmendmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "wo_amendments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WoAmendment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "amendment_type", nullable = false)
    private AmendmentType amendmentType;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_by")
    private String createdBy;
}
