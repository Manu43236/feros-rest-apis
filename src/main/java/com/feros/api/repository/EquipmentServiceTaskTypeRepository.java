package com.feros.api.repository;

import com.feros.api.entity.master.EquipmentServiceTaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentServiceTaskTypeRepository extends JpaRepository<EquipmentServiceTaskType, Long> {
    List<EquipmentServiceTaskType> findAllByIsActiveTrueOrderByNameAsc();
}
