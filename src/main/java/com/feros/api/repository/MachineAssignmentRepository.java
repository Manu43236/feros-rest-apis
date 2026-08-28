package com.feros.api.repository;

import com.feros.api.entity.MachineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MachineAssignmentRepository extends JpaRepository<MachineAssignment, Long> {

    List<MachineAssignment> findByWorkOrderIdOrderByStartDateAsc(Long workOrderId);
    Optional<MachineAssignment> findByIdAndWorkOrderId(Long id, Long workOrderId);
    boolean existsByEquipmentIdAndIsActiveTrue(Long equipmentId);
    Optional<MachineAssignment> findFirstByEquipmentIdAndIsActiveTrue(Long equipmentId);
    long countByWorkOrderId(Long workOrderId);
    List<MachineAssignment> findByWorkOrder_Client_IdAndWorkOrder_Tenant_Id(Long clientId, Long tenantId);
    Optional<MachineAssignment> findByIdAndWorkOrder_Tenant_Id(Long id, Long tenantId);

    // Machine detail — JOIN FETCH workOrder + client to avoid N+1
    @Query("SELECT a FROM MachineAssignment a JOIN FETCH a.workOrder wo JOIN FETCH wo.client WHERE a.equipment.id = :equipmentId AND wo.tenant.id = :tenantId ORDER BY a.startDate DESC")
    List<MachineAssignment> findHistoryByEquipmentId(@Param("equipmentId") Long equipmentId, @Param("tenantId") Long tenantId);

    List<MachineAssignment> findByEquipment_IdAndWorkOrder_Tenant_Id(Long equipmentId, Long tenantId);

    // Operator's active assignment for today (used by operator mobile home screen)
    @Query("""
        SELECT a FROM MachineAssignment a
        JOIN FETCH a.workOrder wo
        JOIN FETCH wo.client
        JOIN FETCH a.equipment eq
        JOIN FETCH eq.equipmentType
        WHERE a.operatorStaff.user.id = :userId
          AND wo.tenant.id = :tenantId
          AND a.isActive = true
          AND a.startDate <= :today
          AND (a.endDate IS NULL OR a.endDate >= :today)
        ORDER BY a.startDate DESC
        """)
    List<MachineAssignment> findActiveByOperatorUserId(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId,
            @Param("today") LocalDate today);
}
