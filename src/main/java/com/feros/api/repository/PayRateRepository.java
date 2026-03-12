package com.feros.api.repository;

import com.feros.api.entity.master.PayRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayRateRepository extends JpaRepository<PayRate, Long> {
    List<PayRate> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<PayRate> findByIdAndTenantId(Long id, Long tenantId);
}