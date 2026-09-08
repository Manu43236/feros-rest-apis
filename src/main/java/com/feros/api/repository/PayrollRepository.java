package com.feros.api.repository;

import com.feros.api.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        SELECT p FROM Payroll p
        JOIN p.user u
        JOIN u.roles r
        WHERE p.tenant.id = :tenantId AND p.isActive = true
        AND (:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:status IS NULL OR p.payrollStatus = :status)
        AND (:role IS NULL OR r.name = :role)
        AND (:month IS NULL OR MONTH(p.payCycleStartDate) = :month)
        AND (:year IS NULL OR YEAR(p.payCycleStartDate) = :year)
        ORDER BY p.payCycleStartDate DESC, p.id DESC
    """)
    Page<Payroll> findAllPaged(
            @Param("tenantId") Long tenantId,
            @Param("search") String search,
            @Param("status") com.feros.api.enums.PayrollStatus status,
            @Param("role") com.feros.api.enums.RoleName role,
            @Param("month") Integer month,
            @Param("year") Integer year,
            Pageable pageable);

    List<Payroll> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);

    Optional<Payroll> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    boolean existsByUserIdAndTenantIdAndPayCycleStartDateAndIsActiveTrue(
            Long userId, Long tenantId, LocalDate startDate);

    boolean existsByUserIdAndTenantIdAndPayCycleStartDateAndPayrollStatusInAndIsActiveTrue(
            Long userId, Long tenantId, LocalDate startDate, List<com.feros.api.enums.PayrollStatus> statuses);

    Optional<Payroll> findByUserIdAndTenantIdAndPayCycleStartDateAndPayrollStatus(
            Long userId, Long tenantId, LocalDate startDate, com.feros.api.enums.PayrollStatus status);

    @Query("""
        SELECT p FROM Payroll p
        WHERE p.user.id = :userId
          AND p.tenant.id = :tenantId
          AND p.isActive = true
          AND p.payrollStatus IN :statuses
          AND p.payCycleStartDate <= :endDate
          AND p.payCycleEndDate >= :startDate
    """)
    Optional<Payroll> findOverlappingPayroll(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<com.feros.api.enums.PayrollStatus> statuses);

    @Query("SELECT p FROM Payroll p WHERE p.tenant.id = :tenantId AND p.isActive = true AND p.payCycleStartDate >= :from AND p.payCycleEndDate <= :to ORDER BY p.payCycleStartDate DESC")
    List<Payroll> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
        SELECT p FROM Payroll p
        WHERE p.user.id = :userId
          AND p.tenant.id = :tenantId
          AND p.isActive = true
          AND p.payCycleStartDate <= :endDate
          AND p.payCycleEndDate >= :startDate
        ORDER BY p.payCycleStartDate
    """)
    List<Payroll> findAllOverlappingByUser(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}