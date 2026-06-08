package com.feros.api.repository;

import com.feros.api.entity.SparePartsTransaction;
import com.feros.api.enums.StockReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SparePartsTransactionRepository extends JpaRepository<SparePartsTransaction, Long> {
    List<SparePartsTransaction> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<SparePartsTransaction> findByTenantIdAndSparePartIdOrderByCreatedAtDesc(Long tenantId, Long sparePartId);
    List<SparePartsTransaction> findByTenantIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long tenantId, LocalDateTime from, LocalDateTime to);
    Optional<SparePartsTransaction> findTopByTenantIdAndSparePartIdAndReferenceTypeOrderByCreatedAtDesc(Long tenantId, Long sparePartId, StockReferenceType referenceType);
    List<SparePartsTransaction> findByServicePart_IdIn(List<Long> servicePartIds);
}
