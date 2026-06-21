package com.feros.api.repository;

import com.feros.api.entity.VehicleService;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleServiceRepository extends JpaRepository<VehicleService, Long> {
    List<VehicleService> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);
    List<VehicleService> findByTenantIdAndServiceDateBetweenAndIsActiveTrue(Long tenantId, LocalDate from, LocalDate to);

    @Query("SELECT s FROM VehicleService s WHERE s.tenant.id = :tenantId AND s.isActive = true " +
           "AND COALESCE(s.serviceDate, s.completedDate) BETWEEN :from AND :to")
    List<VehicleService> findForMaintenanceCostReport(@Param("tenantId") Long tenantId,
                                                      @Param("from") LocalDate from,
                                                      @Param("to") LocalDate to);
    List<VehicleService> findByTenantIdAndVehicleIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, Long vehicleId);
    Optional<VehicleService> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByBreakdownIdAndIsActiveTrueAndStatusNot(Long breakdownId, ServiceStatus status);
    Optional<VehicleService> findFirstByBreakdownIdAndIsActiveTrue(Long breakdownId);

    @Query("SELECT s FROM VehicleService s WHERE s.tenant.id = :tenantId AND s.isActive = true " +
           "AND s.triggeredBy != :excludeTrigger AND s.status IN :statuses ORDER BY s.createdAt DESC")
    List<VehicleService> findActiveGeneralServices(@Param("tenantId") Long tenantId,
                                                    @Param("excludeTrigger") ServiceTriggeredBy excludeTrigger,
                                                    @Param("statuses") List<ServiceStatus> statuses);

    // Find open/in-progress services with due_at_odometer within alert range of current reading
    @Query("SELECT s FROM VehicleService s WHERE s.vehicle.id = :vehicleId AND s.isActive = true " +
           "AND s.status IN ('OPEN','IN_PROGRESS') AND s.dueAtOdometer IS NOT NULL " +
           "AND s.dueAtOdometer <= :alertThreshold")
    List<VehicleService> findServicesNearOrOverdue(@Param("vehicleId") Long vehicleId,
                                                   @Param("alertThreshold") int alertThreshold);
}
