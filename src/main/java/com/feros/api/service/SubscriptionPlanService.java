package com.feros.api.service;

import com.feros.api.dto.request.SubscriptionPlanRequest;
import com.feros.api.dto.response.SubscriptionPlanResponse;

import java.util.List;

public interface SubscriptionPlanService {
    SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request);
    SubscriptionPlanResponse updatePlan(Long id, SubscriptionPlanRequest request);
    List<SubscriptionPlanResponse> getAllPlans();
    List<SubscriptionPlanResponse> getActivePlans();
    void togglePlanStatus(Long id);
}
