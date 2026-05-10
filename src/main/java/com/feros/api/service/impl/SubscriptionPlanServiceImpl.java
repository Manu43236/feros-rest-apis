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
                .pricePerVehicle(request.getPricePerVehicle())
                .minVehicles(request.getMinVehicles())
                .maxVehicles(request.getMaxVehicles())
                .maxLorries(request.getMaxLorries() != null ? request.getMaxLorries() : -1)
                .maxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : -1)
                .priceMonthly(java.math.BigDecimal.ZERO)
                .priceYearly(java.math.BigDecimal.ZERO)
                .hasFuelLogs(request.getHasFuelLogs() != null ? request.getHasFuelLogs() : true)
                .hasMeterReadings(request.getHasMeterReadings() != null ? request.getHasMeterReadings() : true)
                .hasVehicleServices(request.getHasVehicleServices() != null ? request.getHasVehicleServices() : true)
                .hasAttendance(request.getHasAttendance() != null ? request.getHasAttendance() : true)
                .hasPayroll(request.getHasPayroll() != null ? request.getHasPayroll() : true)
                .hasInventory(request.getHasInventory() != null ? request.getHasInventory() : true)
                .hasReports(request.getHasReports() != null ? request.getHasReports() : true)
                .hasCreditNotes(request.getHasCreditNotes() != null ? request.getHasCreditNotes() : true)
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
        if (request.getPricePerVehicle() != null) plan.setPricePerVehicle(request.getPricePerVehicle());
        if (request.getMinVehicles() != null)     plan.setMinVehicles(request.getMinVehicles());
        if (request.getMaxVehicles() != null)     plan.setMaxVehicles(request.getMaxVehicles());
        if (request.getMaxLorries() != null)      plan.setMaxLorries(request.getMaxLorries());
        if (request.getMaxUsers() != null)        plan.setMaxUsers(request.getMaxUsers());
        if (request.getHasFuelLogs() != null)        plan.setHasFuelLogs(request.getHasFuelLogs());
        if (request.getHasMeterReadings() != null)   plan.setHasMeterReadings(request.getHasMeterReadings());
        if (request.getHasVehicleServices() != null) plan.setHasVehicleServices(request.getHasVehicleServices());
        if (request.getHasAttendance() != null)      plan.setHasAttendance(request.getHasAttendance());
        if (request.getHasPayroll() != null)         plan.setHasPayroll(request.getHasPayroll());
        if (request.getHasInventory() != null)       plan.setHasInventory(request.getHasInventory());
        if (request.getHasReports() != null)         plan.setHasReports(request.getHasReports());
        if (request.getHasCreditNotes() != null)     plan.setHasCreditNotes(request.getHasCreditNotes());
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
                .pricePerVehicle(plan.getPricePerVehicle())
                .minVehicles(plan.getMinVehicles())
                .maxVehicles(plan.getMaxVehicles())
                .hasFuelLogs(plan.getHasFuelLogs())
                .hasMeterReadings(plan.getHasMeterReadings())
                .hasVehicleServices(plan.getHasVehicleServices())
                .hasAttendance(plan.getHasAttendance())
                .hasPayroll(plan.getHasPayroll())
                .hasInventory(plan.getHasInventory())
                .hasReports(plan.getHasReports())
                .hasCreditNotes(plan.getHasCreditNotes())
                .features(plan.getFeatures())
                .isActive(plan.getIsActive())
                .build();
    }
}
