package com.feros.api.repository;

import com.feros.api.entity.SalaryAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryAdvanceRepository extends JpaRepository<SalaryAdvance, Long> {
    List<SalaryAdvance> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<SalaryAdvance> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);
    Optional<SalaryAdvance> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    List<SalaryAdvance> findByUserIdAndTenantIdAndIsFullyRepaidFalseAndIsActiveTrue(
            Long userId, Long tenantId);
}