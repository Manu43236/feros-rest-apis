package com.feros.api.repository;

import com.feros.api.entity.VehicleStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleStaffAssignmentRepository extends JpaRepository<VehicleStaffAssignment, Long> {

    // Current open assignment for a user (assigned_to is null)
    Optional<VehicleStaffAssignment> findByUserIdAndTenantIdAndAssignedToIsNullAndIsActiveTrue(
            Long userId, Long tenantId);

    // All assignments for a user that overlap with the given date range (for payroll)
    @Query("""
            SELECT a FROM VehicleStaffAssignment a
            WHERE a.user.id = :userId
              AND a.tenant.id = :tenantId
              AND a.isActive = true
              AND a.assignedFrom <= :endDate
              AND (a.assignedTo IS NULL OR a.assignedTo >= :startDate)
            """)
    List<VehicleStaffAssignment> findOverlappingByUser(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // All assignments for a vehicle that overlap with the given date range (for salary expense report)
    @Query("""
            SELECT a FROM VehicleStaffAssignment a
            WHERE a.vehicle.id = :vehicleId
              AND a.tenant.id = :tenantId
              AND a.isActive = true
              AND a.assignedFrom <= :endDate
              AND (a.assignedTo IS NULL OR a.assignedTo >= :startDate)
            """)
    List<VehicleStaffAssignment> findByVehicleIdAndPeriod(
            @Param("vehicleId") Long vehicleId,
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM VehicleStaffAssignment a " +
           "LEFT JOIN FETCH a.vehicle LEFT JOIN FETCH a.user " +
           "LEFT JOIN FETCH a.assignedBy LEFT JOIN FETCH a.unassignedBy " +
           "WHERE a.tenant.id = :tenantId ORDER BY a.createdAt DESC")
    List<VehicleStaffAssignment> findAllByTenantIdOrderByCreatedAtDesc(@Param("tenantId") Long tenantId);
}
