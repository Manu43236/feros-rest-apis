package com.feros.api.service.impl;

import com.feros.api.dto.response.MechanicSummaryResponse;
import com.feros.api.dto.response.ServiceManagerDashboardResponse;
import com.feros.api.entity.VehicleBreakdown;
import com.feros.api.entity.VehicleService;
import com.feros.api.entity.VehicleServiceTask;
import com.feros.api.enums.BreakdownStatus;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import com.feros.api.repository.UserRepository;
import com.feros.api.repository.VehicleBreakdownRepository;
import com.feros.api.repository.VehicleServiceRepository;
import com.feros.api.service.ServiceManagerService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceManagerServiceImpl implements ServiceManagerService {

    private final VehicleBreakdownRepository breakdownRepository;
    private final VehicleServiceRepository serviceRepository;
    private final UserRepository userRepository;

    @Override
    public ServiceManagerDashboardResponse getDashboard() {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        // Active breakdowns (REPORTED or IN_REPAIR)
        List<VehicleBreakdown> activeBreakdowns = breakdownRepository
                .findByTenantIdAndStatusInAndIsActiveTrueOrderByBreakdownDateDesc(
                        tenantId, List.of(BreakdownStatus.REPORTED, BreakdownStatus.IN_REPAIR));

        // Active general services (not breakdown-triggered)
        List<VehicleService> generalServices = serviceRepository
                .findActiveGeneralServices(tenantId, ServiceTriggeredBy.BREAKDOWN,
                        List.of(ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS));

        List<ServiceManagerDashboardResponse.BreakdownItem> breakdownItems = activeBreakdowns.stream()
                .map(bd -> {
                    VehicleService linked = serviceRepository
                            .findFirstByBreakdownIdAndIsActiveTrue(bd.getId())
                            .orElse(null);
                    List<ServiceManagerDashboardResponse.TaskItem> tasks = mapTaskItems(
                            linked != null && linked.getTasks() != null ? linked.getTasks() : List.of());

                    return ServiceManagerDashboardResponse.BreakdownItem.builder()
                            .breakdownId(bd.getId())
                            .vehicleId(bd.getVehicle().getId())
                            .vehicleRegistrationNumber(bd.getVehicle().getRegistrationNumber())
                            .breakdownType(bd.getBreakdownType())
                            .location(bd.getLocation())
                            .breakdownStatus(bd.getStatus())
                            .breakdownDate(bd.getBreakdownDate())
                            .serviceId(linked != null ? linked.getId() : null)
                            .serviceNumber(linked != null ? linked.getServiceNumber() : null)
                            .serviceStatus(linked != null ? linked.getStatus() : null)
                            .tasksTotal(tasks.size())
                            .tasksAssigned(countAssigned(tasks))
                            .tasksMechanicClosed(countMechanicClosed(tasks))
                            .tasks(tasks)
                            .build();
                }).toList();

        List<ServiceManagerDashboardResponse.ServiceItem> serviceItems = generalServices.stream()
                .map(vs -> {
                    List<ServiceManagerDashboardResponse.TaskItem> tasks = mapTaskItems(
                            vs.getTasks() != null ? vs.getTasks() : List.of());
                    return ServiceManagerDashboardResponse.ServiceItem.builder()
                            .serviceId(vs.getId())
                            .serviceNumber(vs.getServiceNumber())
                            .vehicleId(vs.getVehicle().getId())
                            .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                            .serviceStatus(vs.getStatus())
                            .displayStatus(computeDisplayStatus(vs))
                            .serviceDate(vs.getServiceDate())
                            .triggeredBy(vs.getTriggeredBy())
                            .tasksTotal(tasks.size())
                            .tasksAssigned(countAssigned(tasks))
                            .tasksMechanicClosed(countMechanicClosed(tasks))
                            .tasks(tasks)
                            .build();
                }).toList();

        return ServiceManagerDashboardResponse.builder()
                .breakdowns(breakdownItems)
                .generalServices(serviceItems)
                .build();
    }

    @Override
    public List<MechanicSummaryResponse> getMechanics() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return userRepository.findByTenantIdAndRoleNames(tenantId, List.of(RoleName.MECHANIC))
                .stream()
                .map(u -> MechanicSummaryResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .phone(u.getPhone())
                        .userNumber(u.getUserNumber())
                        .build())
                .toList();
    }

    private List<ServiceManagerDashboardResponse.TaskItem> mapTaskItems(List<VehicleServiceTask> tasks) {
        return tasks.stream()
                .map(t -> ServiceManagerDashboardResponse.TaskItem.builder()
                        .taskId(t.getId())
                        .displayName(t.getTaskType() != null ? t.getTaskType().getName() : t.getCustomName())
                        .status(t.getStatus())
                        .assignedMechanicId(t.getAssignedMechanic() != null ? t.getAssignedMechanic().getId() : null)
                        .assignedMechanicName(t.getAssignedMechanic() != null ? t.getAssignedMechanic().getName() : null)
                        .build())
                .toList();
    }

    private int countAssigned(List<ServiceManagerDashboardResponse.TaskItem> tasks) {
        return (int) tasks.stream()
                .filter(t -> t.getStatus() == ServiceTaskStatus.ASSIGNED
                          || t.getStatus() == ServiceTaskStatus.IN_PROGRESS
                          || t.getStatus() == ServiceTaskStatus.MECHANIC_CLOSED
                          || t.getStatus() == ServiceTaskStatus.COMPLETED)
                .count();
    }

    private int countMechanicClosed(List<ServiceManagerDashboardResponse.TaskItem> tasks) {
        return (int) tasks.stream()
                .filter(t -> t.getStatus() == ServiceTaskStatus.MECHANIC_CLOSED)
                .count();
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
}
