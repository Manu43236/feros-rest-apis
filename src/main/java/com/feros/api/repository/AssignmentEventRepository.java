package com.feros.api.repository;

import com.feros.api.entity.AssignmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentEventRepository extends JpaRepository<AssignmentEvent, Long> {
    List<AssignmentEvent> findByVehicleIdAndTenantIdOrderByPerformedAtDesc(Long vehicleId, Long tenantId);
    List<AssignmentEvent> findByOrderIdAndTenantIdOrderByPerformedAtDesc(Long orderId, Long tenantId);
}
