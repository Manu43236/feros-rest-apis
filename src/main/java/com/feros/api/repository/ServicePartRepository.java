package com.feros.api.repository;

import com.feros.api.entity.ServicePart;
import com.feros.api.enums.ServicePartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePartRepository extends JpaRepository<ServicePart, Long> {
    List<ServicePart> findByServiceIdOrderByCreatedAtDesc(Long serviceId);
    List<ServicePart> findByService_TenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, ServicePartStatus status);
    List<ServicePart> findByService_TenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<ServicePart> findByIdAndService_TenantId(Long id, Long tenantId);
    List<ServicePart> findByService_TenantIdAndService_ServiceDateBetweenAndStatus(Long tenantId, LocalDate from, LocalDate to, ServicePartStatus status);
    List<ServicePart> findByService_TenantIdAndService_ServiceDateBetween(Long tenantId, LocalDate from, LocalDate to);
}
