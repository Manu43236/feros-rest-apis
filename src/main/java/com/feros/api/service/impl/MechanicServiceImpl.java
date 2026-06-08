package com.feros.api.service.impl;

import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.MechanicVehicleTasksResponse;
import com.feros.api.dto.response.ServicePartResponse;
import com.feros.api.entity.VehicleService;
import com.feros.api.entity.VehicleServiceTask;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.VehicleServiceTaskRepository;
import com.feros.api.service.InventoryService;
import com.feros.api.service.MechanicService;
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
public class MechanicServiceImpl implements MechanicService {

    private final VehicleServiceTaskRepository taskRepository;
    private final InventoryService inventoryService;

    @Override
    public List<MechanicVehicleTasksResponse> getMyTasks() {
        Long mechanicId = SecurityUtil.getCurrentUserId();
        Long tenantId   = SecurityUtil.getCurrentTenantId();

        // Fetch all active (non-completed, non-pending) tasks assigned to this mechanic
        List<VehicleServiceTask> tasks = taskRepository.findAssignedToMechanic(
                mechanicId, tenantId,
                List.of(ServiceTaskStatus.ASSIGNED, ServiceTaskStatus.IN_PROGRESS, ServiceTaskStatus.MECHANIC_CLOSED));

        // Group by service
        Map<Long, List<VehicleServiceTask>> byService = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getService().getId()));

        return byService.values().stream()
                .map(serviceTasks -> {
                    VehicleService vs = serviceTasks.get(0).getService();
                    List<MechanicVehicleTasksResponse.MechanicTaskItem> taskItems = serviceTasks.stream()
                            .map(this::toTaskItem)
                            .toList();

                    return MechanicVehicleTasksResponse.builder()
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
                            .tasks(taskItems)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public MechanicVehicleTasksResponse startTask(Long taskId) {
        VehicleServiceTask task = findOwnedTask(taskId);

        if (task.getStatus() != ServiceTaskStatus.ASSIGNED) {
            throw new FerosException(
                    "Task must be in ASSIGNED status to start (current: " + task.getStatus() + ")",
                    HttpStatus.BAD_REQUEST);
        }

        task.setStatus(ServiceTaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        return buildServiceResponse(task.getService(), task.getService().getTasks());
    }

    @Override
    @Transactional
    public MechanicVehicleTasksResponse closeTask(Long taskId) {
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

        // Build a task-linked request and delegate to inventory service
        ServicePartRequest taskRequest = ServicePartRequest.builder()
                .sparePartId(request.getSparePartId())
                .quantityRequested(request.getQuantityRequested())
                .taskId(taskId)
                .build();
        return inventoryService.requestPart(task.getService().getId(), taskRequest);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private VehicleServiceTask findOwnedTask(Long taskId) {
        Long mechanicId = SecurityUtil.getCurrentUserId();
        return taskRepository.findByIdAndAssignedMechanicId(taskId, mechanicId)
                .orElseThrow(() -> new FerosException("Task not found or not assigned to you", HttpStatus.NOT_FOUND));
    }

    private MechanicVehicleTasksResponse buildServiceResponse(VehicleService vs, List<VehicleServiceTask> allTasks) {
        Long mechanicId = SecurityUtil.getCurrentUserId();
        List<MechanicVehicleTasksResponse.MechanicTaskItem> myTasks = allTasks.stream()
                .filter(t -> t.getAssignedMechanic() != null
                          && t.getAssignedMechanic().getId().equals(mechanicId))
                .map(this::toTaskItem)
                .toList();

        return MechanicVehicleTasksResponse.builder()
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
                .tasks(myTasks)
                .build();
    }

    private MechanicVehicleTasksResponse.MechanicTaskItem toTaskItem(VehicleServiceTask t) {
        return MechanicVehicleTasksResponse.MechanicTaskItem.builder()
                .taskId(t.getId())
                .displayName(t.getTaskType() != null ? t.getTaskType().getName() : t.getCustomName())
                .status(t.getStatus())
                .mechanicClosedAt(t.getMechanicClosedAt())
                .build();
    }
}
