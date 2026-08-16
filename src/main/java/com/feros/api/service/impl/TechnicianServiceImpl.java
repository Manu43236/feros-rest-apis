package com.feros.api.service.impl;

import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.TechnicianVehicleTasksResponse;
import com.feros.api.dto.response.ServicePartResponse;
import com.feros.api.entity.EquipmentServicePart;
import com.feros.api.entity.EquipmentServiceRecord;
import com.feros.api.entity.EquipmentServiceTask;
import com.feros.api.entity.ServicePart;
import com.feros.api.entity.User;
import com.feros.api.entity.VehicleService;
import com.feros.api.entity.VehicleServiceTask;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.ServicePartStatus;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentServicePartRepository;
import com.feros.api.repository.EquipmentServiceTaskRepository;
import com.feros.api.repository.ServicePartRepository;
import com.feros.api.repository.SparePartRepository;
import com.feros.api.repository.VehicleServiceTaskRepository;
import com.feros.api.service.InventoryService;
import com.feros.api.service.NotificationService;
import com.feros.api.service.TechnicianService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianServiceImpl implements TechnicianService {

    private final VehicleServiceTaskRepository taskRepository;
    private final ServicePartRepository servicePartRepository;
    private final InventoryService inventoryService;
    // E5 KAN-29
    private final EquipmentServiceTaskRepository equipmentTaskRepository;
    private final EquipmentServicePartRepository equipmentServicePartRepository;
    private final SparePartRepository sparePartRepository;
    private final NotificationService notificationService;

    @Override
    public List<TechnicianVehicleTasksResponse> getMyTasks() {
        Long technicianId = SecurityUtil.getCurrentUserId();
        Long tenantId     = SecurityUtil.getCurrentTenantId();

        List<TechnicianVehicleTasksResponse> result = new ArrayList<>();

        // Vehicle tasks (existing)
        List<VehicleServiceTask> vehicleTasks = taskRepository.findAssignedToMechanic(
                technicianId, tenantId,
                List.of(ServiceTaskStatus.ASSIGNED, ServiceTaskStatus.IN_PROGRESS, ServiceTaskStatus.MECHANIC_CLOSED));

        Map<Long, List<VehicleServiceTask>> byService = vehicleTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getService().getId()));

        List<Long> vehicleTaskIds = vehicleTasks.stream().map(VehicleServiceTask::getId).toList();
        Map<Long, List<ServicePart>> partsByVehicleTask = vehicleTaskIds.isEmpty() ? Map.of()
                : servicePartRepository.findByServiceTaskIdIn(vehicleTaskIds).stream()
                        .collect(Collectors.groupingBy(p -> p.getServiceTask().getId()));

        byService.values().forEach(serviceTasks -> {
            VehicleService vs = serviceTasks.get(0).getService();
            List<TechnicianVehicleTasksResponse.TechnicianTaskItem> taskItems = serviceTasks.stream()
                    .map(t -> toVehicleTaskItem(t, partsByVehicleTask.getOrDefault(t.getId(), List.of())))
                    .toList();
            result.add(TechnicianVehicleTasksResponse.builder()
                    .vehicleId(vs.getVehicle().getId())
                    .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                    .assetType("VEHICLE")
                    .assetName(vs.getVehicle().getRegistrationNumber())
                    .serviceId(vs.getId())
                    .serviceNumber(vs.getServiceNumber())
                    .triggeredBy(vs.getTriggeredBy())
                    .serviceLocation(vs.getLocation())
                    .serviceStatus(vs.getStatus())
                    .breakdownId(vs.getBreakdown() != null ? vs.getBreakdown().getId() : null)
                    .breakdownType(vs.getBreakdown() != null ? vs.getBreakdown().getBreakdownType().name() : null)
                    .breakdownReason(vs.getBreakdown() != null ? vs.getBreakdown().getReason() : null)
                    .tasks(taskItems)
                    .build());
        });

        // E5 KAN-29 — Equipment tasks
        List<EquipmentServiceTask> equipmentTasks = equipmentTaskRepository
                .findByAssignedMechanicIdAndStatusIn(technicianId,
                        List.of(ServiceTaskStatus.ASSIGNED, ServiceTaskStatus.IN_PROGRESS, ServiceTaskStatus.MECHANIC_CLOSED));

        Map<Long, List<EquipmentServiceTask>> byEquipmentService = equipmentTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getServiceRecord().getId()));

        byEquipmentService.values().forEach(eqTasks -> {
            EquipmentServiceRecord sr = eqTasks.get(0).getServiceRecord();
            var eq = sr.getEquipment();
            String assetName = eq.getSerialNumber() != null ? eq.getSerialNumber()
                    : eq.getEquipmentType().getName();
            List<TechnicianVehicleTasksResponse.TechnicianTaskItem> taskItems = eqTasks.stream()
                    .map(this::toEquipmentTaskItem).toList();
            result.add(TechnicianVehicleTasksResponse.builder()
                    .assetType("MACHINE")
                    .assetName(assetName)
                    .serviceId(sr.getId())
                    .serviceNumber(sr.getServiceNumber())
                    .triggeredBy(sr.getTriggeredBy())
                    .serviceLocation(sr.getLocation())
                    .serviceStatus(sr.getStatus())
                    .tasks(taskItems)
                    .build());
        });

        return result;
    }

    @Override
    @Transactional
    public TechnicianVehicleTasksResponse startTask(Long taskId) {
        Long technicianId = SecurityUtil.getCurrentUserId();

        // Try vehicle task first
        var vehicleTask = taskRepository.findByIdAndAssignedMechanicId(taskId, technicianId);
        if (vehicleTask.isPresent()) {
            VehicleServiceTask task = vehicleTask.get();
            if (task.getStatus() != ServiceTaskStatus.ASSIGNED)
                throw new FerosException("Task must be ASSIGNED to start (current: " + task.getStatus() + ")", HttpStatus.BAD_REQUEST);
            task.setStatus(ServiceTaskStatus.IN_PROGRESS);
            task.setMechanicStartedAt(TimeUtil.nowIst());
            taskRepository.save(task);
            return buildVehicleServiceResponse(task.getService(), task.getService().getTasks());
        }

        // Equipment task
        EquipmentServiceTask eqTask = equipmentTaskRepository.findByIdAndAssignedMechanicId(taskId, technicianId)
                .orElseThrow(() -> new FerosException("Task not found or not assigned to you", HttpStatus.NOT_FOUND));
        if (eqTask.getStatus() != ServiceTaskStatus.ASSIGNED)
            throw new FerosException("Task must be ASSIGNED to start (current: " + eqTask.getStatus() + ")", HttpStatus.BAD_REQUEST);
        eqTask.setStatus(ServiceTaskStatus.IN_PROGRESS);
        eqTask.setMechanicStartedAt(TimeUtil.nowIst());
        equipmentTaskRepository.save(eqTask);
        return buildEquipmentServiceResponse(eqTask.getServiceRecord());
    }

    @Override
    @Transactional
    public TechnicianVehicleTasksResponse closeTask(Long taskId) {
        Long technicianId = SecurityUtil.getCurrentUserId();

        var vehicleTask = taskRepository.findByIdAndAssignedMechanicId(taskId, technicianId);
        if (vehicleTask.isPresent()) {
            VehicleServiceTask task = vehicleTask.get();
            if (task.getStatus() != ServiceTaskStatus.IN_PROGRESS && task.getStatus() != ServiceTaskStatus.ASSIGNED)
                throw new FerosException("Task must be ASSIGNED or IN_PROGRESS to close (current: " + task.getStatus() + ")", HttpStatus.BAD_REQUEST);
            task.setStatus(ServiceTaskStatus.MECHANIC_CLOSED);
            task.setMechanicClosedAt(TimeUtil.nowIst());
            taskRepository.save(task);

            VehicleService vs = task.getService();
            String vehicleReg = vs.getVehicle().getRegistrationNumber();
            String taskName = task.getTaskType() != null ? task.getTaskType().getName()
                    : (task.getCustomName() != null ? task.getCustomName() : "Task");
            notificationService.sendToRoles(vs.getTenant(),
                    List.of(RoleName.SERVICE_MANAGER),
                    NotificationType.SERVICE_TASK_COMPLETE,
                    "Task Done — " + vehicleReg,
                    taskName + " on " + vehicleReg + " closed by technician.",
                    Map.of("type", "SERVICE_COMPLETE"));

            boolean allDone = vs.getTasks().stream()
                    .allMatch(t -> t.getStatus() == ServiceTaskStatus.COMPLETED
                            || t.getStatus() == ServiceTaskStatus.MECHANIC_CLOSED);
            if (allDone) {
                notificationService.sendToRoles(vs.getTenant(),
                        List.of(RoleName.SERVICE_MANAGER),
                        NotificationType.SERVICE_TASK_COMPLETE,
                        "All Tasks Done — " + vehicleReg,
                        "All tasks completed on " + vehicleReg + ". Review and release the vehicle.",
                        Map.of("type", "SERVICE_COMPLETE"));
            }

            return buildVehicleServiceResponse(task.getService(), task.getService().getTasks());
        }

        EquipmentServiceTask eqTask = equipmentTaskRepository.findByIdAndAssignedMechanicId(taskId, technicianId)
                .orElseThrow(() -> new FerosException("Task not found or not assigned to you", HttpStatus.NOT_FOUND));
        if (eqTask.getStatus() != ServiceTaskStatus.IN_PROGRESS && eqTask.getStatus() != ServiceTaskStatus.ASSIGNED)
            throw new FerosException("Task must be ASSIGNED or IN_PROGRESS to close (current: " + eqTask.getStatus() + ")", HttpStatus.BAD_REQUEST);
        eqTask.setStatus(ServiceTaskStatus.MECHANIC_CLOSED);
        eqTask.setMechanicClosedAt(TimeUtil.nowIst());
        equipmentTaskRepository.save(eqTask);
        return buildEquipmentServiceResponse(eqTask.getServiceRecord());
    }

    @Override
    @Transactional
    public ServicePartResponse requestSparePart(Long taskId, ServicePartRequest request) {
        Long technicianId = SecurityUtil.getCurrentUserId();

        var vehicleTask = taskRepository.findByIdAndAssignedMechanicId(taskId, technicianId);
        if (vehicleTask.isPresent()) {
            VehicleServiceTask task = vehicleTask.get();
            if (task.getStatus() == ServiceTaskStatus.MECHANIC_CLOSED || task.getStatus() == ServiceTaskStatus.COMPLETED)
                throw new FerosException("Cannot request parts for a closed task", HttpStatus.BAD_REQUEST);
            return inventoryService.requestPart(task.getService().getId(),
                    ServicePartRequest.builder()
                            .sparePartId(request.getSparePartId())
                            .quantityRequested(request.getQuantityRequested())
                            .taskId(taskId)
                            .build());
        }

        // Equipment task part request
        EquipmentServiceTask eqTask = equipmentTaskRepository.findByIdAndAssignedMechanicId(taskId, technicianId)
                .orElseThrow(() -> new FerosException("Task not found or not assigned to you", HttpStatus.NOT_FOUND));
        if (eqTask.getStatus() == ServiceTaskStatus.MECHANIC_CLOSED || eqTask.getStatus() == ServiceTaskStatus.COMPLETED)
            throw new FerosException("Cannot request parts for a closed task", HttpStatus.BAD_REQUEST);

        var sparePart = sparePartRepository.findById(request.getSparePartId())
                .orElseThrow(() -> new FerosException("Spare part not found", HttpStatus.NOT_FOUND));
        User requester = eqTask.getAssignedMechanic();
        EquipmentServicePart part = EquipmentServicePart.builder()
                .service(eqTask.getServiceRecord())
                .serviceTask(eqTask)
                .sparePart(sparePart)
                .quantityRequested(request.getQuantityRequested())
                .status(ServicePartStatus.REQUESTED)
                .requestedBy(requester)
                .build();
        equipmentServicePartRepository.save(part);

        return ServicePartResponse.builder()
                .sparePartId(sparePart.getId())
                .partName(sparePart.getName())
                .partNumber(sparePart.getPartNumber())
                .quantityRequested(request.getQuantityRequested())
                .status(ServicePartStatus.REQUESTED)
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private TechnicianVehicleTasksResponse buildVehicleServiceResponse(VehicleService vs, List<VehicleServiceTask> allTasks) {
        Long technicianId = SecurityUtil.getCurrentUserId();
        List<VehicleServiceTask> myTasks = allTasks.stream()
                .filter(t -> t.getAssignedMechanic() != null && t.getAssignedMechanic().getId().equals(technicianId))
                .toList();
        List<Long> taskIds = myTasks.stream().map(VehicleServiceTask::getId).toList();
        Map<Long, List<ServicePart>> partsByTask = taskIds.isEmpty() ? Map.of()
                : servicePartRepository.findByServiceTaskIdIn(taskIds).stream()
                        .collect(Collectors.groupingBy(p -> p.getServiceTask().getId()));
        return TechnicianVehicleTasksResponse.builder()
                .vehicleId(vs.getVehicle().getId())
                .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                .assetType("VEHICLE")
                .assetName(vs.getVehicle().getRegistrationNumber())
                .serviceId(vs.getId())
                .serviceNumber(vs.getServiceNumber())
                .triggeredBy(vs.getTriggeredBy())
                .serviceLocation(vs.getLocation())
                .serviceStatus(vs.getStatus())
                .breakdownId(vs.getBreakdown() != null ? vs.getBreakdown().getId() : null)
                .breakdownType(vs.getBreakdown() != null ? vs.getBreakdown().getBreakdownType().name() : null)
                .breakdownReason(vs.getBreakdown() != null ? vs.getBreakdown().getReason() : null)
                .tasks(myTasks.stream().map(t -> toVehicleTaskItem(t, partsByTask.getOrDefault(t.getId(), List.of()))).toList())
                .build();
    }

    private TechnicianVehicleTasksResponse buildEquipmentServiceResponse(EquipmentServiceRecord sr) {
        Long technicianId = SecurityUtil.getCurrentUserId();
        var eq = sr.getEquipment();
        String assetName = eq.getSerialNumber() != null ? eq.getSerialNumber() : eq.getEquipmentType().getName();
        List<EquipmentServiceTask> myTasks = sr.getTasks().stream()
                .filter(t -> t.getAssignedMechanic() != null && t.getAssignedMechanic().getId().equals(technicianId))
                .toList();
        return TechnicianVehicleTasksResponse.builder()
                .assetType("MACHINE")
                .assetName(assetName)
                .serviceId(sr.getId())
                .serviceNumber(sr.getServiceNumber())
                .triggeredBy(sr.getTriggeredBy())
                .serviceLocation(sr.getLocation())
                .serviceStatus(sr.getStatus())
                .tasks(myTasks.stream().map(this::toEquipmentTaskItem).toList())
                .build();
    }

    private TechnicianVehicleTasksResponse.TechnicianTaskItem toVehicleTaskItem(VehicleServiceTask t, List<ServicePart> parts) {
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

    private TechnicianVehicleTasksResponse.TechnicianTaskItem toEquipmentTaskItem(EquipmentServiceTask t) {
        List<TechnicianVehicleTasksResponse.PartItem> partItems = equipmentServicePartRepository
                .findByServiceTaskId(t.getId()).stream()
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
                .displayName(t.getCustomName())
                .status(t.getStatus())
                .mechanicStartedAt(t.getMechanicStartedAt())
                .mechanicClosedAt(t.getMechanicClosedAt())
                .parts(partItems)
                .build();
    }
}
