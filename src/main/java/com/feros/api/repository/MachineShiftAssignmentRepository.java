package com.feros.api.repository;

import com.feros.api.entity.MachineShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MachineShiftAssignmentRepository extends JpaRepository<MachineShiftAssignment, Long> {

    // Upsert key: one row per (machine assignment, shift, day)
    Optional<MachineShiftAssignment> findByMachineAssignmentIdAndWorkOrderShiftIdAndWorkDate(
            Long machineAssignmentId, Long workOrderShiftId, LocalDate workDate);

    // Same-date assignments for a staff member (+shift windows) — for the time-overlap guard
    @Query("""
        SELECT a FROM MachineShiftAssignment a JOIN FETCH a.workOrderShift
        WHERE a.operatorStaff.id = :staffId AND a.workDate = :date
        """)
    List<MachineShiftAssignment> findByOperatorStaffIdAndWorkDate(
            @Param("staffId") Long staffId, @Param("date") LocalDate date);

    @Query("""
        SELECT a FROM MachineShiftAssignment a JOIN FETCH a.workOrderShift
        WHERE a.cleanerStaff.id = :staffId AND a.workDate = :date
        """)
    List<MachineShiftAssignment> findByCleanerStaffIdAndWorkDate(
            @Param("staffId") Long staffId, @Param("date") LocalDate date);

    // Grid for the work order detail page
    @Query("""
        SELECT a FROM MachineShiftAssignment a
        JOIN FETCH a.workOrderShift s
        JOIN FETCH a.machineAssignment ma
        JOIN FETCH ma.equipment
        LEFT JOIN FETCH a.operatorStaff os LEFT JOIN FETCH os.user
        LEFT JOIN FETCH a.cleanerStaff cs LEFT JOIN FETCH cs.user
        WHERE ma.workOrder.id = :workOrderId
          AND a.workDate BETWEEN :from AND :to
        ORDER BY a.workDate ASC, s.shiftNumber ASC
        """)
    List<MachineShiftAssignment> findByWorkOrderIdAndWorkDateBetween(
            @Param("workOrderId") Long workOrderId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Operator's shift crew rows for a given day — operator-app "what machine am I on today" lookup.
    @Query("""
        SELECT a FROM MachineShiftAssignment a
        JOIN FETCH a.machineAssignment ma
        JOIN FETCH ma.equipment
        JOIN FETCH ma.workOrder
        WHERE a.operatorStaff.user.id = :userId AND a.workDate = :date
        ORDER BY a.workOrderShift.shiftNumber ASC
        """)
    List<MachineShiftAssignment> findByOperatorUserIdAndWorkDate(
            @Param("userId") Long userId, @Param("date") LocalDate date);

    // Tenant-wide range load for the machine-day resolver + shift-pay resolver.
    @Query("""
        SELECT a FROM MachineShiftAssignment a
        JOIN FETCH a.workOrderShift s
        JOIN FETCH a.machineAssignment ma
        JOIN FETCH ma.equipment
        LEFT JOIN FETCH a.operatorStaff os LEFT JOIN FETCH os.user
        LEFT JOIN FETCH a.cleanerStaff cs LEFT JOIN FETCH cs.user
        WHERE a.tenant.id = :tenantId
          AND a.workDate BETWEEN :from AND :to
        """)
    List<MachineShiftAssignment> findOverlappingByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
