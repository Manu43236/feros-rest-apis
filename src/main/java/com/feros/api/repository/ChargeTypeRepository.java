package com.feros.api.repository;

import com.feros.api.entity.master.ChargeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChargeTypeRepository extends JpaRepository<ChargeType, Long> {
    List<ChargeType> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<ChargeType> findByIdAndTenantId(Long id, Long tenantId);
}