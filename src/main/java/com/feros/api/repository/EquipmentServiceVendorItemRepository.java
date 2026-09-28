package com.feros.api.repository;

import com.feros.api.entity.EquipmentServiceVendorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentServiceVendorItemRepository extends JpaRepository<EquipmentServiceVendorItem, Long> {
    List<EquipmentServiceVendorItem> findByServiceRecordIdOrderByIdAsc(Long serviceId);
}
