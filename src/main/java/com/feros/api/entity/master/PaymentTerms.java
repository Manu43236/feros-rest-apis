package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTerms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "credit_days")
    private Integer creditDays = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;
}