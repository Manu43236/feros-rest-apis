package com.feros.api.repository;

import com.feros.api.entity.TireIssueRequest;
import com.feros.api.enums.TireRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TireRequestRepository extends JpaRepository<TireIssueRequest, Long> {

    List<TireIssueRequest> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    List<TireIssueRequest> findByTenantIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId, TireRequestStatus status);
}
