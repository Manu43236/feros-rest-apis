package com.feros.api.entity;


import com.feros.api.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "designations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Designation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private RoleName roleType;

    @Column(name = "pay_per_day")
    private BigDecimal payPerDay;

    @Builder.Default
    @Column(name = "monthly_leave_quota")
    private Integer monthlyLeaveQuota = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;
}