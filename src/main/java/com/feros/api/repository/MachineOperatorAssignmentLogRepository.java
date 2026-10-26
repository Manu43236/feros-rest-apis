package com.feros.api.repository;

import com.feros.api.entity.MachineOperatorAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MachineOperatorAssignmentLogRepository extends JpaRepository<MachineOperatorAssignmentLog, Long> {

    // Current open log for a machine assignment
    Optional<MachineOperatorAssignmentLog> findByMachineAssignmentIdAndUnassignedAtIsNull(Long machineAssignmentId);

    // Active log for an operator across all machines (one-machine check)
    @Query("""
        SELECT l FROM MachineOperatorAssignmentLog l
        WHERE l.operatorStaff.id = :staffId
          AND l.tenant.id = :tenantId
          AND l.unassignedAt IS NULL
        """)
    Optional<MachineOperatorAssignmentLog> findActiveByOperatorStaffId(
            @Param("staffId") Long staffId,
            @Param("tenantId") Long tenantId);

    // Logs overlapping a date range for ONE operator — payroll machine-day resolution + payslip.
    @Query("""
        SELECT l FROM MachineOperatorAssignmentLog l
        JOIN FETCH l.machineAssignment a
        JOIN FETCH a.equipment
        JOIN FETCH l.operatorStaff os
        JOIN FETCH os.user
        WHERE l.tenant.id = :tenantId
          AND os.user.id = :userId
          AND l.assignedAt <= :endDate
          AND (l.unassignedAt IS NULL OR l.unassignedAt >= :startDate)
        """)
    List<MachineOperatorAssignmentLog> findOverlappingByOperatorUserId(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Tenant-wide overlapping range — reports + resolver bulk load
    @Query("""
        SELECT l FROM MachineOperatorAssignmentLog l
        JOIN FETCH l.machineAssignment a
        JOIN FETCH a.equipment
        JOIN FETCH a.workOrder
        JOIN FETCH l.operatorStaff os
        JOIN FETCH os.user
        WHERE l.tenant.id = :tenantId
          AND l.operatorStaff IS NOT NULL
          AND l.assignedAt <= :endDate
          AND (l.unassignedAt IS NULL OR l.unassignedAt >= :startDate)
        """)
    List<MachineOperatorAssignmentLog> findOverlappingByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // History for a work order — newest first
    @Query("""
        SELECT l FROM MachineOperatorAssignmentLog l
        LEFT JOIN FETCH l.machineAssignment a
        LEFT JOIN FETCH a.equipment
        LEFT JOIN FETCH l.operatorStaff os
        LEFT JOIN FETCH os.user
        LEFT JOIN FETCH l.assignedBy
        WHERE a.workOrder.id = :workOrderId
        ORDER BY l.assignedAt DESC
        """)
    List<MachineOperatorAssignmentLog> findByWorkOrderIdForHistory(@Param("workOrderId") Long workOrderId);
}
