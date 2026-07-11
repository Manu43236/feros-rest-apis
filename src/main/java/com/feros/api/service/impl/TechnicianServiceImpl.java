package com.feros.api.service.impl;

import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.TechnicianVehicleTasksResponse;
import com.feros.api.dto.response.ServicePartResponse;
import com.feros.api.entity.ServicePart;
import com.feros.api.entity.VehicleService;
import com.feros.api.entity.VehicleServiceTask;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.ServicePartRepository;
import com.feros.api.repository.VehicleServiceTaskRepository;
import com.feros.api.service.InventoryService;
import com.feros.api.service.TechnicianService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianServiceImpl implements TechnicianService {

    private final VehicleServiceTaskRepository taskRepository;
    private final ServicePartRepository servicePartRepository;
    private final InventoryService inventoryService;

    @Override
    public List<TechnicianVehicleTasksResponse> getMyTasks() {
        Long technicianId = SecurityUtil.getCurrentUserId();
        Long tenantId     = SecurityUtil.getCurrentTenantId();

        List<VehicleServiceTask> tasks = taskRepository.findAssignedToMechanic(
                technicianId, tenantId,
                List.of(ServiceTaskStatus.ASSIGNED, ServiceTaskStatus.IN_PROGRESS, ServiceTaskStatus.MECHANIC_CLOSED));

        Map<Long, List<VehicleServiceTask>> byService = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getService().getId()));

        List<Long> allTaskIds = tasks.stream().map(VehicleServiceTask::getId).toList();
        Map<Long, List<ServicePart>> partsByTask = allTaskIds.isEmpty() ? Map.of()
                : servicePartRepository.findByServiceTaskIdIn(allTaskIds).stream()
                        .collect(Collectors.groupingBy(p -> p.getServiceTask().getId()));

        return byService.values().stream()
                .map(serviceTasks -> {
                    VehicleService vs = serviceTasks.get(0).getService();
                    List<TechnicianVehicleTasksResponse.TechnicianTaskItem> taskItems = serviceTasks.stream()
                            .map(t -> toTaskItem(t, partsByTask.getOrDefault(t.getId(), List.of())))
                            .toList();

                    return TechnicianVehicleTasksResponse.builder()
                            .vehicleId(vs.getVehicle().getId())
                            .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                            .serviceId(vs.getId())
                            .serviceNumber(vs.getServiceNumber())
                            .triggeredBy(vs.getTriggeredBy())
                            .serviceLocation(vs.getLocation())
                            .serviceStatus(vs.getStatus())
                            .breakdownId(vs.getBreakdown() != null ? vs.getBreakdown().getId() : null)
                            .breakdownType(vs.getBreakdown() != null
                                    ? vs.getBreakdown().getBreakdownType().name() : null)
                            .breakdownReason(vs.getBreakdown() != null ? vs.getBreakdown().getReason() : null)
                            .tasks(taskItems)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public TechnicianVehicleTasksResponse startTask(Long taskId) {
        VehicleServiceTask task = findOwnedTask(taskId);

        if (task.getStatus() != ServiceTaskStatus.ASSIGNED) {
            throw new FerosException(
                    "Task must be in ASSIGNED status to start (current: " + task.getStatus() + ")",
                    HttpStatus.BAD_REQUEST);
        }

        task.setStatus(ServiceTaskStatus.IN_PROGRESS);
        task.setMechanicStartedAt(TimeUtil.nowIst());
        taskRepository.save(task);

        return buildServiceResponse(task.getService(), task.getService().getTasks());
    }

    @Override
    @Transactional
    public TechnicianVehicleTasksResponse closeTask(Long taskId) {
        VehicleServiceTask task = findOwnedTask(taskId);

        if (task.getStatus() != ServiceTaskStatus.IN_PROGRESS
                && task.getStatus() != ServiceTaskStatus.ASSIGNED) {
            throw new FerosException(
                    "Task must be ASSIGNED or IN_PROGRESS to close (current: " + task.getStatus() + ")",
                    HttpStatus.BAD_REQUEST);
        }

        task.setStatus(ServiceTaskStatus.MECHANIC_CLOSED);
        task.setMechanicClosedAt(TimeUtil.nowIst());
        taskRepository.save(task);

        return buildServiceResponse(task.getService(), task.getService().getTasks());
    }

    @Override
    @Transactional
    public ServicePartResponse requestSparePart(Long taskId, ServicePartRequest request) {
        VehicleServiceTask task = findOwnedTask(taskId);

        if (task.getStatus() == ServiceTaskStatus.MECHANIC_CLOSED
                || task.getStatus() == ServiceTaskStatus.COMPLETED) {
            throw new FerosException("Cannot request parts for a closed task", HttpStatus.BAD_REQUEST);
        }

        ServicePartRequest taskRequest = ServicePartRequest.builder()
                .sparePartId(request.getSparePartId())
                .quantityRequested(request.getQuantityRequested())
                .taskId(taskId)
                .build();
        return inventoryService.requestPart(task.getService().getId(), taskRequest);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private VehicleServiceTask findOwnedTask(Long taskId) {
        Long technicianId = SecurityUtil.getCurrentUserId();
        return taskRepository.findByIdAndAssignedMechanicId(taskId, technicianId)
                .orElseThrow(() -> new FerosException("Task not found or not assigned to you", HttpStatus.NOT_FOUND));
    }

    private TechnicianVehicleTasksResponse buildServiceResponse(VehicleService vs, List<VehicleServiceTask> allTasks) {
        Long technicianId = SecurityUtil.getCurrentUserId();
        List<VehicleServiceTask> myTasksList = allTasks.stream()
                .filter(t -> t.getAssignedMechanic() != null
                          && t.getAssignedMechanic().getId().equals(technicianId))
                .toList();
        List<Long> taskIds = myTasksList.stream().map(VehicleServiceTask::getId).toList();
        Map<Long, List<ServicePart>> partsByTask = taskIds.isEmpty() ? Map.of()
                : servicePartRepository.findByServiceTaskIdIn(taskIds).stream()
                        .collect(Collectors.groupingBy(p -> p.getServiceTask().getId()));
        List<TechnicianVehicleTasksResponse.TechnicianTaskItem> myTasks = myTasksList.stream()
                .map(t -> toTaskItem(t, partsByTask.getOrDefault(t.getId(), List.of())))
                .toList();

        return TechnicianVehicleTasksResponse.builder()
                .vehicleId(vs.getVehicle().getId())
                .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                .serviceId(vs.getId())
                .serviceNumber(vs.getServiceNumber())
                .triggeredBy(vs.getTriggeredBy())
                .serviceLocation(vs.getLocation())
                .serviceStatus(vs.getStatus())
                .breakdownId(vs.getBreakdown() != null ? vs.getBreakdown().getId() : null)
                .breakdownType(vs.getBreakdown() != null
                        ? vs.getBreakdown().getBreakdownType().name() : null)
                .breakdownReason(vs.getBreakdown() != null ? vs.getBreakdown().getReason() : null)
                .tasks(myTasks)
                .build();
    }

    private TechnicianVehicleTasksResponse.TechnicianTaskItem toTaskItem(VehicleServiceTask t, List<ServicePart> parts) {
        List<TechnicianVehicleTasksResponse.PartItem> partItems = parts.stream()
                .map(p -> TechnicianVehicleTasksResponse.PartItem.builder()
                        .partId(p.getSparePart().getId())
                        .partName(p.getSparePart().getName())
                        .partNumber(p.getSparePart().getPartNumber())
                        .quantityRequested(p.getQuantityRequested())
                        .quantityApproved(p.getQuantityApproved())
                        .status(p.getStatus())
                        .build())
                .toList();

        return TechnicianVehicleTasksResponse.TechnicianTaskItem.builder()
                .taskId(t.getId())
                .displayName(t.getTaskType() != null ? t.getTaskType().getName() : t.getCustomName())
                .status(t.getStatus())
                .mechanicStartedAt(t.getMechanicStartedAt())
                .mechanicClosedAt(t.getMechanicClosedAt())
                .parts(partItems)
                .build();
    }
}
