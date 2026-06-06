package com.feros.api.repository;

import com.feros.api.entity.GpsProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GpsProviderConfigRepository extends JpaRepository<GpsProviderConfig, Long> {

    List<GpsProviderConfig> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    Optional<GpsProviderConfig> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
}
