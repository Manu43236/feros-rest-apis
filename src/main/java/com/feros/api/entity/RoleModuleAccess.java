package com.feros.api.entity;

import com.feros.api.enums.ModuleKey;
import com.feros.api.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "role_module_access",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "role", "module_key"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleModuleAccess extends BaseEntity {

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
    @Column(name = "module_key", nullable = false, length = 30)
    private ModuleKey moduleKey;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
}
