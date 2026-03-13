package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lr_checkposts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LrCheckpost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lr_id", nullable = false)
    private Lr lr;

    @Column(name = "checkpost_name", nullable = false)
    private String checkpostName;

    @Column(name = "location")
    private String location;

    @Column(name = "fine_amount")
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "fine_receipt_number")
    private String fineReceiptNumber;

    @Column(name = "fine_paid_at")
    private LocalDateTime finePaidAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive = true;
}