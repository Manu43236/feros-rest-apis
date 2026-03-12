package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active")
    private Boolean isActive = true;
}