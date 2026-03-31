package com.feros.api.repository;

import com.feros.api.entity.ServicePart;
import com.feros.api.enums.ServicePartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePartRepository extends JpaRepository<ServicePart, Long> {
    List<ServicePart> findByServiceIdOrderByCreatedAtDesc(Long serviceId);
    List<ServicePart> findByService_TenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, ServicePartStatus status);
    Optional<ServicePart> findByIdAndService_TenantId(Long id, Long tenantId);
}
