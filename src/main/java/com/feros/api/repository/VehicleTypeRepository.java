package com.feros.api.repository;

import com.feros.api.entity.master.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {
    List<VehicleType> findAllByIsActiveTrue();
}