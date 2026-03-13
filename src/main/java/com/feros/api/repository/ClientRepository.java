package com.feros.api.repository;

import com.feros.api.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Client> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    boolean existsByPhoneAndTenantId(String phone, Long tenantId);
}