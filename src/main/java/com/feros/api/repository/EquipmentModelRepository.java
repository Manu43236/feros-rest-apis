package com.feros.api.repository;

import com.feros.api.entity.master.EquipmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentModelRepository extends JpaRepository<EquipmentModel, Long> {
    List<EquipmentModel> findAllByIsActiveTrue();
    List<EquipmentModel> findAllByMakeIdAndIsActiveTrue(Long makeId);
}
