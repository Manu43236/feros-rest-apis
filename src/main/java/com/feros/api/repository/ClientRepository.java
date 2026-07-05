package com.feros.api.repository;

import com.feros.api.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<Client> findByTenantId(Long tenantId);
    Optional<Client> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);
    Optional<Client> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByPhoneAndTenantId(String phone, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);

    @Query("""
        SELECT c FROM Client c
        WHERE c.tenant.id = :tenantId
        AND (:search IS NULL
             OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))
             OR c.phone LIKE CONCAT('%', :search, '%'))
        ORDER BY c.clientName ASC
    """)
    Page<Client> findAllPaged(
            @Param("tenantId") Long tenantId,
            @Param("search") String search,
            Pageable pageable);
}