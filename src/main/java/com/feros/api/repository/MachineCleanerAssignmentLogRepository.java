package com.feros.api.repository;

import com.feros.api.entity.MachineCleanerAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MachineCleanerAssignmentLogRepository extends JpaRepository<MachineCleanerAssignmentLog, Long> {

    Optional<MachineCleanerAssignmentLog> findByMachineAssignmentIdAndUnassignedAtIsNull(Long machineAssignmentId);

    @Query("""
        SELECT l FROM MachineCleanerAssignmentLog l
        WHERE l.cleanerStaff.id = :staffId
          AND l.tenant.id = :tenantId
          AND l.unassignedAt IS NULL
        """)
    Optional<MachineCleanerAssignmentLog> findActiveByCleanerStaffId(
            @Param("staffId") Long staffId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT l FROM MachineCleanerAssignmentLog l
        JOIN FETCH l.machineAssignment a
        JOIN FETCH a.equipment
        JOIN FETCH l.cleanerStaff cs
        JOIN FETCH cs.user
        WHERE l.tenant.id = :tenantId
          AND cs.user.id = :userId
          AND l.assignedAt <= :endDate
          AND (l.unassignedAt IS NULL OR l.unassignedAt >= :startDate)
        """)
    List<MachineCleanerAssignmentLog> findOverlappingByCleanerUserId(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT l FROM MachineCleanerAssignmentLog l
        JOIN FETCH l.machineAssignment a
        JOIN FETCH a.equipment
        JOIN FETCH a.workOrder
        JOIN FETCH l.cleanerStaff cs
        JOIN FETCH cs.user
        WHERE l.tenant.id = :tenantId
          AND l.cleanerStaff IS NOT NULL
          AND l.assignedAt <= :endDate
          AND (l.unassignedAt IS NULL OR l.unassignedAt >= :startDate)
        """)
    List<MachineCleanerAssignmentLog> findOverlappingByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT l FROM MachineCleanerAssignmentLog l
        LEFT JOIN FETCH l.machineAssignment a
        LEFT JOIN FETCH a.equipment
        LEFT JOIN FETCH l.cleanerStaff cs
        LEFT JOIN FETCH cs.user
        LEFT JOIN FETCH l.assignedBy
        WHERE a.workOrder.id = :workOrderId
        ORDER BY l.assignedAt DESC
        """)
    List<MachineCleanerAssignmentLog> findByWorkOrderIdForHistory(@Param("workOrderId") Long workOrderId);
}
