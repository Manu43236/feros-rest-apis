package com.feros.api.repository;

import com.feros.api.entity.master.VehicleBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Long> {
    List<VehicleBrand> findAllByIsActiveTrue();
    java.util.Optional<VehicleBrand> findByNameIgnoreCase(String name);
}