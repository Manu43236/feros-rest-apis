package com.feros.api.repository;

import com.feros.api.entity.ClientAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAdvanceRepository extends JpaRepository<ClientAdvance, Long> {
    List<ClientAdvance> findByTenantIdAndIsActiveTrueOrderByReceivedDateDesc(Long tenantId);
    List<ClientAdvance> findByTenantIdAndClientIdAndIsActiveTrueOrderByReceivedDateDesc(Long tenantId, Long clientId);
    Optional<ClientAdvance> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}
