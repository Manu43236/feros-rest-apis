package com.feros.api.repository;

import com.feros.api.entity.Lr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LrRepository extends JpaRepository<Lr, Long> {
    List<Lr> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Lr> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByVehicleAllocationId(Long vehicleAllocationId);
    java.util.Optional<Lr> findByVehicleAllocationId(Long vehicleAllocationId);
    List<Lr> findByOrderIdAndIsActiveTrue(Long orderId);
    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND l.lrDate BETWEEN :from AND :to ORDER BY l.lrDate DESC")
    List<Lr> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND l.lrDate BETWEEN :from AND :to AND l.order.client.id = :clientId ORDER BY l.lrDate DESC")
    List<Lr> findByTenantIdAndDateRangeAndClient(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("clientId") Long clientId);
}