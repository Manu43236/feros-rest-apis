package com.feros.api.repository;

import com.feros.api.entity.EquipmentDailyLog;
import com.feros.api.enums.DailyLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    List<EquipmentDailyLog> findByWorkOrderIdAndLogDateBetween(Long workOrderId, LocalDate from, LocalDate to);
    List<EquipmentDailyLog> findByMachineAssignmentIdInAndLogDateBetween(List<Long> assignmentIds, LocalDate from, LocalDate to);
    List<EquipmentDailyLog> findByMachineAssignmentIdIn(List<Long> assignmentIds);

    @Query("SELECT COALESCE(SUM(l.hoursWorked), 0) FROM EquipmentDailyLog l JOIN l.machineAssignment a JOIN a.workOrder wo WHERE wo.tenant.id = :tenantId AND l.logDate = :date")
    BigDecimal sumHoursByTenantAndDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(l.hoursWorked), 0) FROM EquipmentDailyLog l JOIN l.machineAssignment a JOIN a.workOrder wo WHERE wo.tenant.id = :tenantId AND l.logDate BETWEEN :from AND :to")
    BigDecimal sumHoursByTenantAndDateRange(@Param("tenantId") Long tenantId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
