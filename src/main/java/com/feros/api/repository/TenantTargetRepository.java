package com.feros.api.repository;

import com.feros.api.entity.TenantTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantTargetRepository extends JpaRepository<TenantTarget, Long> {
    Optional<TenantTarget> findByTenantIdAndYearAndMonth(Long tenantId, Integer year, Integer month);
    List<TenantTarget> findByTenantIdOrderByYearDescMonthDesc(Long tenantId);
}
