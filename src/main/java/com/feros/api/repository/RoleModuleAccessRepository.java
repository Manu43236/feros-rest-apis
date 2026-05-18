package com.feros.api.repository;

import com.feros.api.entity.RoleModuleAccess;
import com.feros.api.enums.ModuleKey;
import com.feros.api.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RoleModuleAccessRepository extends JpaRepository<RoleModuleAccess, Long> {

    List<RoleModuleAccess> findByTenantId(Long tenantId);

    List<RoleModuleAccess> findByTenantIdAndRole(Long tenantId, RoleName role);

    List<RoleModuleAccess> findByTenantIdAndRoleAndEnabledTrue(Long tenantId, RoleName role);

    boolean existsByTenantIdAndModuleKey(Long tenantId, ModuleKey moduleKey);

    @Modifying
    @Transactional
    @Query("DELETE FROM RoleModuleAccess r WHERE r.tenant.id = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);
}
