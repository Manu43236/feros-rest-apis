package com.feros.api.repository;

import com.feros.api.entity.Tyre;
import com.feros.api.enums.TyreStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TyreRepository extends JpaRepository<Tyre, Long> {

    List<Tyre> findByTenantIdAndIsActiveTrueOrderByIdDesc(Long tenantId);

    List<Tyre> findByTenantIdAndStatusAndIsActiveTrueOrderByIdDesc(Long tenantId, TyreStatus status);

    Optional<Tyre> findFirstByTenantIdAndStatusAndIsActiveTrueOrderByIdAsc(Long tenantId, TyreStatus status);

    Optional<Tyre> findByTenantIdAndSerialNumberAndIsActiveTrue(Long tenantId, String serialNumber);
}
