package com.feros.api.repository;

import com.feros.api.entity.SparePartsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparePartsTransactionRepository extends JpaRepository<SparePartsTransaction, Long> {
    List<SparePartsTransaction> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<SparePartsTransaction> findByTenantIdAndSparePartIdOrderByCreatedAtDesc(Long tenantId, Long sparePartId);
}
