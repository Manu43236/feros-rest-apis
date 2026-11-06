package com.feros.api.repository;

import com.feros.api.entity.ConsumableIssueRequest;
import com.feros.api.enums.ConsumableRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsumableIssueRequestRepository extends JpaRepository<ConsumableIssueRequest, Long> {

    List<ConsumableIssueRequest> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    List<ConsumableIssueRequest> findByTenantIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, ConsumableRequestStatus status);

    List<ConsumableIssueRequest> findByTenantIdAndRequestedByIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, Long requestedById);
}
