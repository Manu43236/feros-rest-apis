package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tenant_holidays",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "holiday_date"}),
    indexes = @Index(name = "idx_tenant_holidays", columnList = "tenant_id, holiday_date"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantHoliday extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_name", nullable = false, length = 100)
    private String holidayName;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
