package com.feros.api.service;

import com.feros.api.dto.request.TenantTargetRequest;
import com.feros.api.dto.response.TenantTargetResponse;

import java.util.List;

public interface TenantTargetService {
    TenantTargetResponse setTarget(TenantTargetRequest request);
    TenantTargetResponse getTarget(Integer year, Integer month);
    TenantTargetResponse getCurrentMonthTarget();
    List<TenantTargetResponse> getAllTargets();
}
