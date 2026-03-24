package com.feros.api.repository;

import com.feros.api.entity.master.VehicleStatus;
import com.feros.api.enums.VehicleStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleStatusRepository extends JpaRepository<VehicleStatus, Long> {
    List<VehicleStatus> findByIsActiveTrue();
    Optional<VehicleStatus> findByIdAndIsActiveTrue(Long id);
    Optional<VehicleStatus> findByNameIgnoreCaseAndIsActiveTrue(String name);
    Optional<VehicleStatus> findByStatusTypeAndIsActiveTrue(VehicleStatusType statusType);
}
