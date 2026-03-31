package com.feros.api.service.impl;

import com.feros.api.dto.request.CompleteServiceRequest;
import com.feros.api.dto.request.VehicleServiceRequest;
import com.feros.api.dto.request.VehicleServiceTaskRequest;
import com.feros.api.dto.response.VehicleServiceResponse;
import com.feros.api.dto.response.VehicleServiceTaskResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.ServiceTaskType;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import com.feros.api.enums.VehicleServiceType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.VehicleMaintenanceService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleMaintenanceServiceImpl implements VehicleMaintenanceService {

    private final VehicleServiceRepository vehicleServiceRepository;
    private final VehicleServiceTaskRepository vehicleServiceTaskRepository;
    private final VehicleRepository vehicleRepository;
    private final TenantRepository tenantRepository;
    private final VehicleBreakdownRepository vehicleBreakdownRepository;
    private final ServiceTaskTypeRepository serviceTaskTypeRepository;

    @Override
    @Transactional
    public VehicleServiceResponse create(VehicleServiceRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(request.getVehicleId(), tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        VehicleBreakdown breakdown = null;
        if (request.getBreakdownId() != null) {
            breakdown = vehicleBreakdownRepository.findById(request.getBreakdownId())
                    .orElseThrow(() -> new FerosException("Breakdown not found", HttpStatus.NOT_FOUND));
        }

        String vendorName = request.getServiceType() == VehicleServiceType.INTERNAL ? "Self" : request.getVendorName();

        VehicleService vs = VehicleService.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .serviceNumber(NumberUtil.generate(tenant.getPrefix(), tenantId, NumberUtil.Type.SVC))
                .triggeredBy(request.getTriggeredBy())
                .breakdown(breakdown)
                .serviceType(request.getServiceType())
                .vendorName(vendorName)
                .location(request.getLocation())
                .status(ServiceStatus.OPEN)
                .dueAtOdometer(request.getDueAtOdometer())
                .serviceDate(request.getServiceDate())
                .odometer(request.getOdometer())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        VehicleService saved = vehicleServiceRepository.save(vs);

        for (VehicleServiceTaskRequest taskReq : request.getTasks()) {
            ServiceTaskType taskType = null;
            if (taskReq.getTaskTypeId() != null) {
                taskType = serviceTaskTypeRepository.findById(taskReq.getTaskTypeId()).orElse(null);
            }
            VehicleServiceTask task = VehicleServiceTask.builder()
                    .service(saved)
                    .taskType(taskType)
                    .customName(taskReq.getCustomName())
                    .isRecurring(Boolean.TRUE.equals(taskReq.getIsRecurring()))
                    .frequencyKm(taskReq.getFrequencyKm())
                    .cost(taskReq.getCost())
                    .status(ServiceTaskStatus.PENDING)
                    .build();
            vehicleServiceTaskRepository.save(task);
        }

        return mapToResponse(vehicleServiceRepository.findById(saved.getId()).orElse(saved));
    }

    @Override
    public List<VehicleServiceResponse> getAll() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return vehicleServiceRepository
                .findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<VehicleServiceResponse> getByVehicle(Long vehicleId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return vehicleServiceRepository
                .findByTenantIdAndVehicleIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId, vehicleId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public VehicleServiceResponse getById(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return mapToResponse(vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional
    public VehicleServiceResponse start(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (vs.getStatus() != ServiceStatus.OPEN) {
            throw new FerosException("Only OPEN services can be started", HttpStatus.BAD_REQUEST);
        }

        vs.setStatus(ServiceStatus.IN_PROGRESS);
        vs.setStartedAt(java.time.LocalDateTime.now());
        vehicleServiceRepository.save(vs);
        return mapToResponse(vehicleServiceRepository.findById(vs.getId()).orElse(vs));
    }

    @Override
    @Transactional
    public VehicleServiceResponse complete(Long id, CompleteServiceRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (vs.getStatus() == ServiceStatus.COMPLETED) {
            throw new FerosException("Service is already completed", HttpStatus.BAD_REQUEST);
        }
        if (vs.getStatus() == ServiceStatus.OPEN) {
            vs.setStatus(ServiceStatus.IN_PROGRESS); // allow direct OPEN → COMPLETED
        }

        vs.setStatus(ServiceStatus.COMPLETED);
        vs.setCompletedDate(request.getCompletedDate());
        if (request.getOdometer() != null) vs.setOdometer(request.getOdometer());

        vs.getTasks().forEach(t -> t.setStatus(ServiceTaskStatus.COMPLETED));
        vehicleServiceRepository.save(vs);

        // Auto-schedule next services for recurring tasks (SCHEDULED flow only)
        if (vs.getTriggeredBy() == ServiceTriggeredBy.SCHEDULED && vs.getOdometer() != null) {
            autoScheduleNextServices(vs);
        }

        return mapToResponse(vehicleServiceRepository.findById(vs.getId()).orElse(vs));
    }

    @Override
    public void delete(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        vs.setIsActive(false);
        vehicleServiceRepository.save(vs);
    }

    private void autoScheduleNextServices(VehicleService completedService) {
        List<VehicleServiceTask> recurringTasks = completedService.getTasks().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsRecurring()) && t.getFrequencyKm() != null)
                .toList();

        if (recurringTasks.isEmpty()) return;

        Map<Integer, List<VehicleServiceTask>> grouped = recurringTasks.stream()
                .collect(Collectors.groupingBy(VehicleServiceTask::getFrequencyKm));

        for (Map.Entry<Integer, List<VehicleServiceTask>> entry : grouped.entrySet()) {
            int freqKm = entry.getKey();
            List<VehicleServiceTask> tasks = entry.getValue();
            int nextDueOdometer = completedService.getOdometer() + freqKm;

            VehicleService nextService = VehicleService.builder()
                    .tenant(completedService.getTenant())
                    .vehicle(completedService.getVehicle())
                    .serviceNumber(NumberUtil.generate(
                            completedService.getTenant().getPrefix(),
                            completedService.getTenant().getId(),
                            NumberUtil.Type.SVC))
                    .triggeredBy(ServiceTriggeredBy.SCHEDULED)
                    .serviceType(VehicleServiceType.INTERNAL)
                    .vendorName("Self")
                    .status(ServiceStatus.OPEN)
                    .dueAtOdometer(nextDueOdometer)
                    .isActive(true)
                    .build();

            VehicleService savedNext = vehicleServiceRepository.save(nextService);

            for (VehicleServiceTask t : tasks) {
                VehicleServiceTask newTask = VehicleServiceTask.builder()
                        .service(savedNext)
                        .taskType(t.getTaskType())
                        .customName(t.getCustomName())
                        .isRecurring(true)
                        .frequencyKm(freqKm)
                        .status(ServiceTaskStatus.PENDING)
                        .build();
                vehicleServiceTaskRepository.save(newTask);
            }
        }
    }

    private String computeDisplayStatus(VehicleService vs) {
        if (vs.getStatus() == ServiceStatus.COMPLETED) return "COMPLETED";
        if (vs.getStatus() == ServiceStatus.IN_PROGRESS) return "IN_PROGRESS";
        if (vs.getDueAtOdometer() == null) return "OPEN";
        BigDecimal currentOdo = vs.getVehicle().getCurrentOdometerReading();
        int current = currentOdo != null ? currentOdo.intValue() : 0;
        int due = vs.getDueAtOdometer();
        if (current >= due) return "OVERDUE";
        if (due - current <= 1000) return "DUE_SOON";
        return "OPEN";
    }

    private VehicleServiceResponse mapToResponse(VehicleService vs) {
        List<VehicleServiceTask> tasks = vs.getTasks() != null ? vs.getTasks() : new ArrayList<>();

        BigDecimal totalCost = tasks.stream()
                .filter(t -> t.getCost() != null)
                .map(VehicleServiceTask::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<VehicleServiceTaskResponse> taskResponses = tasks.stream()
                .map(t -> VehicleServiceTaskResponse.builder()
                        .id(t.getId())
                        .taskTypeId(t.getTaskType() != null ? t.getTaskType().getId() : null)
                        .taskTypeName(t.getTaskType() != null ? t.getTaskType().getName() : null)
                        .customName(t.getCustomName())
                        .displayName(t.getTaskType() != null ? t.getTaskType().getName() : t.getCustomName())
                        .isRecurring(t.getIsRecurring())
                        .frequencyKm(t.getFrequencyKm())
                        .cost(t.getCost())
                        .status(t.getStatus())
                        .build())
                .toList();

        return VehicleServiceResponse.builder()
                .id(vs.getId())
                .tenantId(vs.getTenant().getId())
                .vehicleId(vs.getVehicle().getId())
                .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                .serviceNumber(vs.getServiceNumber())
                .triggeredBy(vs.getTriggeredBy())
                .breakdownId(vs.getBreakdown() != null ? vs.getBreakdown().getId() : null)
                .serviceType(vs.getServiceType())
                .vendorName(vs.getVendorName())
                .location(vs.getLocation())
                .status(vs.getStatus())
                .displayStatus(computeDisplayStatus(vs))
                .dueAtOdometer(vs.getDueAtOdometer())
                .serviceDate(vs.getServiceDate())
                .completedDate(vs.getCompletedDate())
                .odometer(vs.getOdometer())
                .notes(vs.getNotes())
                .totalCost(totalCost)
                .tasks(taskResponses)
                .startedAt(vs.getStartedAt())
                .createdAt(vs.getCreatedAt())
                .updatedAt(vs.getUpdatedAt())
                .build();
    }
}
