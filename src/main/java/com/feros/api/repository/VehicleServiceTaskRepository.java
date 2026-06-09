package com.feros.api.repository;

import com.feros.api.entity.VehicleServiceTask;
import com.feros.api.enums.ServiceTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleServiceTaskRepository extends JpaRepository<VehicleServiceTask, Long> {
    List<VehicleServiceTask> findByServiceId(Long serviceId);

    @Query("SELECT t FROM VehicleServiceTask t WHERE t.assignedMechanic.id = :mechanicId " +
           "AND t.service.tenant.id = :tenantId AND t.service.isActive = true " +
           "AND t.status IN :statuses ORDER BY t.service.createdAt DESC")
    List<VehicleServiceTask> findAssignedToMechanic(@Param("mechanicId") Long mechanicId,
                                                    @Param("tenantId") Long tenantId,
                                                    @Param("statuses") List<ServiceTaskStatus> statuses);

    Optional<VehicleServiceTask> findByIdAndAssignedMechanicId(Long taskId, Long mechanicId);

    @Query("SELECT t FROM VehicleServiceTask t WHERE t.service.tenant.id = :tenantId " +
           "AND t.service.serviceDate BETWEEN :startDate AND :endDate " +
           "AND t.assignedMechanic IS NOT NULL AND t.service.isActive = true")
    List<VehicleServiceTask> findByTenantIdAndServiceDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
