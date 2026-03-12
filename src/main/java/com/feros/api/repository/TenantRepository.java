package com.feros.api.repository;

import com.feros.api.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Tenant> findByIdAndIsActiveTrue(Long id);

    List<Tenant> findAllByIsActiveTrue();
}