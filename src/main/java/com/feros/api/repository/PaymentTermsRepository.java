package com.feros.api.repository;

import com.feros.api.entity.master.PaymentTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTermsRepository extends JpaRepository<PaymentTerms, Long> {
    List<PaymentTerms> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<PaymentTerms> findByIdAndTenantId(Long id, Long tenantId);
}