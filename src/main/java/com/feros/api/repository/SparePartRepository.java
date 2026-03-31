package com.feros.api.repository;

import com.feros.api.entity.master.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    List<SparePart> findByTenantIdAndIsActiveTrueOrderByNameAsc(Long tenantId);
    Optional<SparePart> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}
