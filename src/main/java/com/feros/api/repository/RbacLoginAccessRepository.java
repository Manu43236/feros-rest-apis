package com.feros.api.repository;

import com.feros.api.entity.RbacLoginAccess;
import com.feros.api.enums.DeviceType;
import com.feros.api.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RbacLoginAccessRepository extends JpaRepository<RbacLoginAccess, Long> {

    List<RbacLoginAccess> findByTenantId(Long tenantId);

    Optional<RbacLoginAccess> findByTenantIdAndRoleAndPlatform(Long tenantId, RoleName role, DeviceType platform);

    void deleteByTenantId(Long tenantId);
}
