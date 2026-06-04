package com.feros.api.repository;

import com.feros.api.entity.TyreIssueRequest;
import com.feros.api.enums.TyreRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TyreRequestRepository extends JpaRepository<TyreIssueRequest, Long> {

    List<TyreIssueRequest> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    List<TyreIssueRequest> findByTenantIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, TyreRequestStatus status);

    List<TyreIssueRequest> findByTenantIdAndRequestedByIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, Long requestedById);

    List<TyreIssueRequest> findByTenantIdAndCreatedAtBetweenAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, java.time.LocalDateTime from, java.time.LocalDateTime to);
}
