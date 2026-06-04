package com.feros.api.repository;

import com.feros.api.entity.LrTripExpense;
import com.feros.api.enums.TripExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LrTripExpenseRepository extends JpaRepository<LrTripExpense, Long> {
    Optional<LrTripExpense> findByLrIdAndIsActiveTrue(Long lrId);
    Optional<LrTripExpense> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByLrIdAndIsActiveTrue(Long lrId);
    List<LrTripExpense> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<LrTripExpense> findByTenantIdAndStatusAndIsActiveTrue(Long tenantId, TripExpenseStatus status);

    @Query("SELECT e FROM LrTripExpense e WHERE e.tenant.id = :tenantId AND e.isActive = true AND e.lr.lrDate BETWEEN :from AND :to ORDER BY e.lr.lrDate DESC")
    List<LrTripExpense> findByTenantIdAndLrDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
