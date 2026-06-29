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
}
