package com.feros.api.repository;

import com.feros.api.entity.Lr;
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
public interface LrRepository extends JpaRepository<Lr, Long> {
    List<Lr> findByTenantIdAndIsActiveTrue(Long tenantId);

    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true " +
           "AND (:status IS NULL OR l.lrStatus = :status) " +
           "AND (:search IS NULL OR LOWER(l.lrNumber) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(l.vehicleAllocation.vehicle.registrationNumber) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Lr> findAllPaged(@Param("tenantId") Long tenantId,
                          @Param("status") com.feros.api.enums.LrStatus status,
                          @Param("search") String search,
                          Pageable pageable);
    Optional<Lr> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByVehicleAllocationId(Long vehicleAllocationId);
    boolean existsByVehicleAllocationIdAndLrStatusNot(Long vehicleAllocationId, com.feros.api.enums.LrStatus status);
    java.util.Optional<Lr> findByVehicleAllocationId(Long vehicleAllocationId);
    List<Lr> findAllByVehicleAllocationId(Long vehicleAllocationId);
    List<Lr> findByOrderIdAndIsActiveTrue(Long orderId);
    List<Lr> findByTenantIdAndCreatedByIdAndIsActiveTrue(Long tenantId, Long createdById);
    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND l.lrDate BETWEEN :from AND :to ORDER BY l.lrDate DESC")
    List<Lr> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND l.lrDate BETWEEN :from AND :to AND l.order.client.id = :clientId ORDER BY l.lrDate DESC")
    List<Lr> findByTenantIdAndDateRangeAndClient(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("clientId") Long clientId);

    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND l.lrStatus = :status")
    List<Lr> findByTenantIdAndLrStatus(@Param("tenantId") Long tenantId, @Param("status") com.feros.api.enums.LrStatus status);

    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND FUNCTION('DATE', l.loadedAt) = :date")
    List<Lr> findByTenantIdAndLoadedAtDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND FUNCTION('DATE', l.deliveredAt) = :date")
    List<Lr> findByTenantIdAndDeliveredAtDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND l.lrStatus = :status AND l.lrDate BETWEEN :from AND :to")
    List<Lr> findByTenantIdAndLrStatusAndDateRange(@Param("tenantId") Long tenantId, @Param("status") com.feros.api.enums.LrStatus status, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND l.isOverloaded = true AND l.lrDate BETWEEN :from AND :to ORDER BY l.lrDate DESC")
    List<Lr> findOverloadedByTenantIdAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT l FROM Lr l WHERE l.tenant.id = :tenantId AND l.isActive = true AND (l.driver.id = :userId OR l.cleaner.id = :userId) ORDER BY l.id DESC")
    List<Lr> findByTenantIdAndDriverUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Query("SELECT l FROM Lr l WHERE l.vehicleAllocation.vehicle.id = :vehicleId AND l.isActive = true AND l.lrStatus NOT IN :excludedStatuses")
    List<Lr> findActiveByVehicleIdExcludingStatuses(@Param("vehicleId") Long vehicleId, @Param("excludedStatuses") List<com.feros.api.enums.LrStatus> excludedStatuses);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Lr l " +
           "WHERE l.vehicleAllocation.vehicle.id = :vehicleId AND l.isActive = true " +
           "AND l.lrStatus = com.feros.api.enums.LrStatus.IN_TRANSIT")
    boolean existsInTransitLrForVehicle(@Param("vehicleId") Long vehicleId);
}