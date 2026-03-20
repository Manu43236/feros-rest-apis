package com.feros.api.service.impl;

import com.feros.api.dto.request.SubscriptionPlanRequest;
import com.feros.api.dto.response.SubscriptionPlanResponse;
import com.feros.api.entity.SubscriptionPlan;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.SubscriptionPlanRepository;
import com.feros.api.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    @Override
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .maxLorries(request.getMaxLorries())
                .maxUsers(request.getMaxUsers())
                .priceMonthly(request.getPriceMonthly())
                .priceYearly(request.getPriceYearly())
                .features(request.getFeatures())
                .isActive(true)
                .build();
        return toResponse(planRepository.save(plan));
    }

    @Override
    public SubscriptionPlanResponse updatePlan(Long id, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new FerosException("Plan not found", HttpStatus.NOT_FOUND));
        plan.setName(request.getName());
        plan.setMaxLorries(request.getMaxLorries());
        plan.setMaxUsers(request.getMaxUsers());
        plan.setPriceMonthly(request.getPriceMonthly());
        plan.setPriceYearly(request.getPriceYearly());
        plan.setFeatures(request.getFeatures());
        return toResponse(planRepository.save(plan));
    }

    @Override
    public List<SubscriptionPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionPlanResponse> getActivePlans() {
        return planRepository.findAllByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void togglePlanStatus(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new FerosException("Plan not found", HttpStatus.NOT_FOUND));
        plan.setIsActive(!plan.getIsActive());
        planRepository.save(plan);
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .maxLorries(plan.getMaxLorries())
                .maxUsers(plan.getMaxUsers())
                .priceMonthly(plan.getPriceMonthly())
                .priceYearly(plan.getPriceYearly())
                .features(plan.getFeatures())
                .isActive(plan.getIsActive())
                .build();
    }
}
