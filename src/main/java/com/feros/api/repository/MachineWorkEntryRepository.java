package com.feros.api.repository;

import com.feros.api.entity.MachineWorkEntry;
import com.feros.api.enums.WorkEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MachineWorkEntryRepository extends JpaRepository<MachineWorkEntry, Long> {

    Optional<MachineWorkEntry> findByMachineAssignmentIdAndStatus(Long machineAssignmentId, WorkEntryStatus status);

    List<MachineWorkEntry> findByMachineAssignmentIdOrderByStartTimeDesc(Long machineAssignmentId);

    boolean existsByMachineAssignmentIdAndStatus(Long machineAssignmentId, WorkEntryStatus status);

    List<MachineWorkEntry> findByMachineAssignment_WorkOrder_IdOrderByStartTimeDesc(Long workOrderId);

    List<MachineWorkEntry> findByMachineAssignment_WorkOrder_IdAndStartTimeBetweenOrderByStartTimeDesc(
            Long workOrderId, java.time.LocalDateTime from, java.time.LocalDateTime to);

    Optional<MachineWorkEntry> findTopByMachineAssignmentIdAndStatusOrderByEndTimeDesc(Long machineAssignmentId, WorkEntryStatus status);

    // For scheduler — all completed entries for a machine assignment on a given day
    @org.springframework.data.jpa.repository.Query(
        "SELECT e FROM MachineWorkEntry e WHERE e.machineAssignment.id = :assignmentId " +
        "AND e.status = 'COMPLETED' AND FUNCTION('DATE', e.startTime) = :date")
    List<MachineWorkEntry> findCompletedByAssignmentAndDate(
        @org.springframework.data.repository.query.Param("assignmentId") Long assignmentId,
        @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);
}
