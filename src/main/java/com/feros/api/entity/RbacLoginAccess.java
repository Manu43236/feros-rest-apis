package com.feros.api.entity;

import com.feros.api.enums.DeviceType;
import com.feros.api.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "rbac_login_access",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "role", "platform"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RbacLoginAccess extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private RoleName role;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 10)
    private DeviceType platform;

    @Column(name = "allowed", nullable = false)
    private Boolean allowed;
}
