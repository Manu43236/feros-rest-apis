package com.feros.api.repository;

import com.feros.api.entity.EquipmentServiceTask;
import com.feros.api.enums.ServiceTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentServiceTaskRepository extends JpaRepository<EquipmentServiceTask, Long> {
    List<EquipmentServiceTask> findByAssignedMechanicId(Long mechanicId);
    Optional<EquipmentServiceTask> findByIdAndAssignedMechanicId(Long id, Long mechanicId);
    // E5 KAN-29
    List<EquipmentServiceTask> findByAssignedMechanicIdAndStatusIn(Long mechanicId, List<ServiceTaskStatus> statuses);
}
