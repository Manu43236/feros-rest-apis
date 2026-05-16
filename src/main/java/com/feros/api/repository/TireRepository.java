package com.feros.api.repository;

import com.feros.api.entity.Tire;
import com.feros.api.enums.TireStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TireRepository extends JpaRepository<Tire, Long> {

    List<Tire> findByTenantIdAndIsActiveTrueOrderByIdDesc(Long tenantId);

    List<Tire> findByTenantIdAndStatusAndIsActiveTrueOrderByIdDesc(Long tenantId, TireStatus status);

    Optional<Tire> findFirstByTenantIdAndStatusAndIsActiveTrueOrderByIdAsc(Long tenantId, TireStatus status);

    Optional<Tire> findByTenantIdAndSerialNumberAndIsActiveTrue(Long tenantId, String serialNumber);
}
