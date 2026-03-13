package com.feros.api.repository;

import com.feros.api.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByTenantIdAndIsActiveTrue(Long tenantId);

    List<Payroll> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);

    Optional<Payroll> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    boolean existsByUserIdAndTenantIdAndPayCycleStartDateAndIsActiveTrue(
            Long userId, Long tenantId, LocalDate startDate);
    @Query("SELECT p FROM Payroll p WHERE p.tenant.id = :tenantId AND p.isActive = true AND p.payCycleStartDate >= :from AND p.payCycleEndDate <= :to ORDER BY p.payCycleStartDate DESC")
    List<Payroll> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}