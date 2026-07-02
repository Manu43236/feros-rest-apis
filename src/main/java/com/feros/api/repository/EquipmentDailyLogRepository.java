package com.feros.api.repository;

import com.feros.api.entity.EquipmentDailyLog;
import com.feros.api.enums.DailyLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EquipmentDailyLogRepository extends JpaRepository<EquipmentDailyLog, Long> {

    List<EquipmentDailyLog> findByWorkOrderIdOrderByLogDateAscIdAsc(Long workOrderId);
    List<EquipmentDailyLog> findByMachineAssignmentId(Long machineAssignmentId);
    List<EquipmentDailyLog> findByWorkOrderIdAndStatus(Long workOrderId, DailyLogStatus status);
    boolean existsByMachineAssignmentIdAndLogDate(Long machineAssignmentId, LocalDate logDate);
    Optional<EquipmentDailyLog> findByIdAndWorkOrderId(Long id, Long workOrderId);
    Optional<EquipmentDailyLog> findTopByMachineAssignmentIdOrderByLogDateDescIdDesc(Long machineAssignmentId);
}
