package com.feros.api.repository;

import com.feros.api.entity.MachineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MachineAssignmentRepository extends JpaRepository<MachineAssignment, Long> {

    List<MachineAssignment> findByWorkOrderIdOrderByStartDateAsc(Long workOrderId);
    Optional<MachineAssignment> findByIdAndWorkOrderId(Long id, Long workOrderId);
    boolean existsByEquipmentIdAndIsActiveTrue(Long equipmentId);
    long countByWorkOrderId(Long workOrderId);
}
