package com.feros.api.repository;

import com.feros.api.entity.MachineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MachineAssignmentRepository extends JpaRepository<MachineAssignment, Long> {

    List<MachineAssignment> findByWorkOrderIdOrderByStartDateAsc(Long workOrderId);
    Optional<MachineAssignment> findByIdAndWorkOrderId(Long id, Long workOrderId);
    boolean existsByEquipmentIdAndIsActiveTrue(Long equipmentId);
    long countByWorkOrderId(Long workOrderId);
    List<MachineAssignment> findByWorkOrder_Client_IdAndWorkOrder_Tenant_Id(Long clientId, Long tenantId);
    Optional<MachineAssignment> findByIdAndWorkOrder_Tenant_Id(Long id, Long tenantId);

    // Machine detail — JOIN FETCH workOrder + client to avoid N+1
    @Query("SELECT a FROM MachineAssignment a JOIN FETCH a.workOrder wo JOIN FETCH wo.client WHERE a.equipment.id = :equipmentId AND wo.tenant.id = :tenantId ORDER BY a.startDate DESC")
    List<MachineAssignment> findHistoryByEquipmentId(@Param("equipmentId") Long equipmentId, @Param("tenantId") Long tenantId);
}
