package com.feros.api.repository;

import com.feros.api.entity.VehicleServiceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleServiceTaskRepository extends JpaRepository<VehicleServiceTask, Long> {
    List<VehicleServiceTask> findByServiceId(Long serviceId);
}
