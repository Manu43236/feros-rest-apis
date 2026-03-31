package com.feros.api.repository;

import com.feros.api.entity.SparePartsInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SparePartsInventoryRepository extends JpaRepository<SparePartsInventory, Long> {
    List<SparePartsInventory> findByTenantIdOrderBySparePartNameAsc(Long tenantId);
    Optional<SparePartsInventory> findByTenantIdAndSparePartId(Long tenantId, Long sparePartId);
}
