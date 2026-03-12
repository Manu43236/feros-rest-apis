package com.feros.api.repository;

import com.feros.api.entity.master.ClientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientTypeRepository extends JpaRepository<ClientType, Long> {
    List<ClientType> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<ClientType> findByIdAndTenantId(Long id, Long tenantId);
}