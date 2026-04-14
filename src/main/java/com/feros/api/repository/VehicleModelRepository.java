package com.feros.api.repository;

import com.feros.api.entity.master.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
    List<VehicleModel> findByBrand_IdAndIsActiveTrueOrderByNameAsc(Long brandId);
    List<VehicleModel> findAllByIsActiveTrueOrderByBrand_NameAscNameAsc();
    boolean existsByBrand_IdAndNameIgnoreCaseAndIsActiveTrue(Long brandId, String name);
}
