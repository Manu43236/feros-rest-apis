package com.feros.api.repository;

import com.feros.api.entity.TenantTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantThemeRepository extends JpaRepository<TenantTheme, Long> {
    Optional<TenantTheme> findByTenantIdAndIsActiveTrue(Long tenantId);
}