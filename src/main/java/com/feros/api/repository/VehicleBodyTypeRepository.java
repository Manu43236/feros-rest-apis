package com.feros.api.repository;

import com.feros.api.entity.master.VehicleBodyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleBodyTypeRepository extends JpaRepository<VehicleBodyType, Long> {
    List<VehicleBodyType> findAllByIsActiveTrue();
}
