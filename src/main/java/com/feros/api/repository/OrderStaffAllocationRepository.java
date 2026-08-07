package com.feros.api.repository;

import com.feros.api.entity.OrderStaffAllocation;
import com.feros.api.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStaffAllocationRepository extends JpaRepository<OrderStaffAllocation, Long> {
    List<OrderStaffAllocation> findByVehicleAllocationIdAndIsActiveTrue(Long vehicleAllocationId);
    List<OrderStaffAllocation> findAllByVehicleAllocationIdOrderByCreatedAtDesc(Long vehicleAllocationId);
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM OrderStaffAllocation sa " +
           "WHERE sa.vehicleAllocation.id = :vehicleAllocationId AND sa.user.id = :userId " +
           "AND sa.isActive = true AND sa.allocationStatus <> com.feros.api.enums.StaffAllocationStatus.CANCELLED")
    boolean existsByVehicleAllocationIdAndUserIdAndIsActiveTrue(@Param("vehicleAllocationId") Long vehicleAllocationId,
                                                                 @Param("userId") Long userId);
    long countByUserIdAndAllocationStatusAndIsActiveTrue(Long userId, com.feros.api.enums.StaffAllocationStatus status);
    Optional<OrderStaffAllocation> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    // Fix: NULL dates treated as open-ended (no boundary) to prevent silent double-booking
    // Note: uses typed enum params — Hibernate 6 requires enum constants, not string literals
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id = :userId AND sa.isActive = true " +
           "AND sa.allocationStatus IN :activeStatuses " +
           "AND (:startDate IS NULL OR sa.expectedEndDate IS NULL OR sa.expectedEndDate >= :startDate) " +
           "AND (:endDate IS NULL OR sa.expectedStartDate IS NULL OR sa.expectedStartDate <= :endDate)")
    boolean existsStaffConflict(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("activeStatuses") List<com.feros.api.enums.StaffAllocationStatus> activeStatuses);

    // Within-order conflict: same user already assigned to a different vehicle in this order
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id = :userId AND sa.isActive = true " +
           "AND sa.allocationStatus IN :activeStatuses " +
           "AND sa.order.id = :orderId " +
           "AND sa.vehicleAllocation.id <> :excludeVehicleAllocationId")
    boolean existsWithinOrderConflict(@Param("userId") Long userId,
                                       @Param("orderId") Long orderId,
                                       @Param("excludeVehicleAllocationId") Long excludeVehicleAllocationId,
                                       @Param("activeStatuses") List<com.feros.api.enums.StaffAllocationStatus> activeStatuses);

    // Active allocation for a user (ALLOCATED or IN_TRANSIT) — used for isAssigned + guards
    @Query("SELECT sa FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id = :userId AND sa.isActive = true " +
           "AND sa.allocationStatus IN :activeStatuses " +
           "ORDER BY sa.createdAt DESC")
    List<OrderStaffAllocation> findActiveAllocationsForUser(@Param("userId") Long userId,
                                                             @Param("activeStatuses") List<com.feros.api.enums.StaffAllocationStatus> activeStatuses);

    // Bulk variants — avoid N+1 in getAllUsers
    @Query("SELECT sa.user.id, COUNT(sa) FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id IN :userIds AND sa.allocationStatus = :status AND sa.isActive = true " +
           "GROUP BY sa.user.id")
    List<Object[]> countCompletedByUserIds(@Param("userIds") List<Long> userIds,
                                            @Param("status") com.feros.api.enums.StaffAllocationStatus status);

    @Query("SELECT sa FROM OrderStaffAllocation sa " +
           "WHERE sa.user.id IN :userIds AND sa.isActive = true " +
           "AND sa.allocationStatus IN :activeStatuses " +
           "ORDER BY sa.createdAt DESC")
    List<OrderStaffAllocation> findActiveAllocationsByUserIds(@Param("userIds") List<Long> userIds,
                                                               @Param("activeStatuses") List<com.feros.api.enums.StaffAllocationStatus> activeStatuses);

    @Query("SELECT sa FROM OrderStaffAllocation sa WHERE sa.tenant.id = :tenantId AND sa.isActive = true " +
           "AND sa.expectedStartDate BETWEEN :from AND :to ORDER BY sa.expectedStartDate DESC")
    List<OrderStaffAllocation> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId,
                                                           @Param("from") LocalDate from,
                                                           @Param("to") LocalDate to);

    @Query("SELECT sa FROM OrderStaffAllocation sa WHERE sa.tenant.id = :tenantId AND sa.isActive = true " +
           "AND sa.order.id = :orderId")
    List<OrderStaffAllocation> findByTenantIdAndOrderId(@Param("tenantId") Long tenantId, @Param("orderId") Long orderId);

    @Query("SELECT sa FROM OrderStaffAllocation sa JOIN FETCH sa.order " +
           "WHERE sa.tenant.id = :tenantId AND sa.isActive = true")
    List<OrderStaffAllocation> findAllByTenantWithOrder(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(DISTINCT sa.user.id) FROM OrderStaffAllocation sa " +
           "WHERE sa.tenant.id = :tenantId AND sa.isActive = true " +
           "AND sa.role.name = :roleName AND sa.allocationStatus = :status")
    long countDistinctUsersByTenantIdAndRoleAndStatus(@Param("tenantId") Long tenantId,
                                                      @Param("roleName") RoleName roleName,
                                                      @Param("status") com.feros.api.enums.StaffAllocationStatus status);

    // Bulk period preload for attendance summary — all active trips in a date range for the tenant
    @Query("SELECT sa FROM OrderStaffAllocation sa " +
           "JOIN FETCH sa.vehicleAllocation va " +
           "JOIN FETCH va.vehicle " +
           "WHERE sa.tenant.id = :tenantId " +
           "AND sa.isActive = true " +
           "AND sa.allocationStatus <> com.feros.api.enums.StaffAllocationStatus.CANCELLED " +
           "AND va.isActive = true " +
           "AND sa.actualStartDate IS NOT NULL AND sa.actualStartDate <= :endDate " +
           "AND (sa.actualEndDate IS NULL OR sa.actualEndDate >= :startDate)")
    List<OrderStaffAllocation> findActiveInPeriodForTenant(@Param("tenantId") Long tenantId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    // Fallback vehicle lookup: standing assignment has lapsed but driver is on an active order trip
    @Query("SELECT sa FROM OrderStaffAllocation sa " +
           "JOIN FETCH sa.vehicleAllocation va " +
           "JOIN FETCH va.vehicle " +
           "WHERE sa.user.id = :userId AND sa.tenant.id = :tenantId " +
           "AND sa.isActive = true " +
           "AND sa.allocationStatus <> com.feros.api.enums.StaffAllocationStatus.CANCELLED " +
           "AND va.isActive = true " +
           "AND sa.actualStartDate IS NOT NULL AND sa.actualStartDate <= :date " +
           "AND (sa.actualEndDate IS NULL OR sa.actualEndDate >= :date)")
    List<OrderStaffAllocation> findActiveOnDateForUser(@Param("userId") Long userId,
                                                       @Param("tenantId") Long tenantId,
                                                       @Param("date") LocalDate date);
}