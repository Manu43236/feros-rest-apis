package com.feros.api.repository;

import com.feros.api.entity.LrTripExpense;
import com.feros.api.enums.TripExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LrTripExpenseRepository extends JpaRepository<LrTripExpense, Long> {
    Optional<LrTripExpense> findByLrIdAndIsActiveTrue(Long lrId);
    Optional<LrTripExpense> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByLrIdAndIsActiveTrue(Long lrId);
    List<LrTripExpense> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<LrTripExpense> findByTenantIdAndStatusAndIsActiveTrue(Long tenantId, TripExpenseStatus status);
}
