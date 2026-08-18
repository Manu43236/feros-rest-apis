package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.CompleteServiceRequest;
import com.feros.api.dto.request.VehicleServiceRequest;
import com.feros.api.dto.request.VehicleServiceTaskRequest;
import com.feros.api.dto.response.ServiceVendorItemResponse;
import com.feros.api.dto.response.VehicleServiceResponse;
import com.feros.api.dto.response.VehicleServiceTaskResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.ServiceTaskType;
import com.feros.api.entity.master.TenantSettings;
import com.feros.api.enums.BreakdownStatus;
import com.feros.api.enums.ServiceInvoiceStatus;
import com.feros.api.enums.ServiceInvoiceType;
import com.feros.api.enums.ServicePartStatus;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import com.feros.api.enums.StockReferenceType;
import com.feros.api.enums.VehicleServiceType;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.exception.FerosException;
import com.feros.api.entity.ServicePart;
import com.feros.api.entity.SparePartsTransaction;
import com.feros.api.entity.User;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.service.S3Service;
import com.feros.api.service.VehicleMaintenanceService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final VehicleStatusRepository vehicleStatusRepository;
    private final ServiceTaskTypeRepository serviceTaskTypeRepository;
    private final ServiceInvoiceRepository serviceInvoiceRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final ServicePartRepository servicePartRepository;
    private final SparePartsTransactionRepository sparePartsTransactionRepository;
    private final UserRepository userRepository;
    private final ServiceVendorItemRepository serviceVendorItemRepository;
    private final NumberGeneratorService numberGenerator;
    private final NotificationService notificationService;
    private final S3Service s3Service;

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

            // Prevent duplicate active service for the same breakdown
            boolean alreadyHasService = vehicleServiceRepository
                    .existsByBreakdownIdAndIsActiveTrueAndStatusNot(request.getBreakdownId(), ServiceStatus.COMPLETED);
            if (alreadyHasService) {
                throw new FerosException(
                        "An active service already exists for this breakdown. Complete it before creating a new one.",
                        HttpStatus.CONFLICT);
            }
        }

        String vendorName = request.getServiceType() == VehicleServiceType.INTERNAL
                ? (request.getVendorName() != null ? request.getVendorName() : "Self")
                : request.getVendorName();

        VehicleService vs = VehicleService.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .serviceNumber(numberGenerator.generateFY(tenantId, NumberUtil.Type.SVC))
                .triggeredBy(request.getTriggeredBy())
                .breakdown(breakdown)
                .serviceType(request.getServiceType())
                .payerType(request.getPayerType() != null
                        ? request.getPayerType()
                        : com.feros.api.enums.ServicePayerType.OWN_EXPENSE)
                .vendorName(vendorName)
                .location(request.getLocation())
                .status(ServiceStatus.OPEN)
                .dueAtOdometer(request.getDueAtOdometer())
                .serviceDate(request.getServiceDate())
                .odometer(request.getOdometer())
                .notes(request.getNotes())
                .insuranceClaimNo(request.getInsuranceClaimNo())
                .insuranceClaimAmt(request.getInsuranceClaimAmt())
                .certificateNumber(request.getCertificateNumber())
                .certificateValidUntil(request.getCertificateValidUntil())
                .isEscalated(Boolean.TRUE.equals(request.getIsEscalated()))
                .estimatedCost(request.getEstimatedCost())
                .isActive(true)
                .build();

        VehicleService saved = vehicleServiceRepository.save(vs);

        String triggerLabel = request.getTriggeredBy() != null
                ? request.getTriggeredBy().name().replace("_", " ") : "Manual";
        int taskCount = request.getTasks() != null ? request.getTasks().size() : 0;
        String taskSuffix = taskCount > 0 ? taskCount + " task(s) added." : "No tasks yet — assign technicians.";
        notificationService.sendToRoles(saved.getTenant(),
                List.of(com.feros.api.enums.RoleName.SERVICE_MANAGER),
                com.feros.api.enums.NotificationType.SERVICE_OPENED,
                "New Service — " + vehicle.getRegistrationNumber(),
                vehicle.getRegistrationNumber() + " | " + triggerLabel + " | " + taskSuffix,
                java.util.Map.of("type", "SERVICE_COMPLETE"));

        // When a service is created for a breakdown → move breakdown to IN_REPAIR + vehicle to IN_REPAIR
        if (breakdown != null) {
            breakdown.setStatus(BreakdownStatus.IN_REPAIR);
            vehicleBreakdownRepository.save(breakdown);

            vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(VehicleStatusType.IN_REPAIR)
                    .ifPresent(inRepairStatus -> {
                        vehicle.setCurrentStatus(inRepairStatus);
                        vehicleRepository.save(vehicle);
                    });
        }

        List<VehicleServiceTaskRequest> taskRequests = request.getTasks() != null ? request.getTasks() : new ArrayList<>();
        for (VehicleServiceTaskRequest taskReq : taskRequests) {
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
        vs.setStartedAt(TimeUtil.nowIst());
        vehicleServiceRepository.save(vs);
        return mapToResponse(vehicleServiceRepository.findById(vs.getId()).orElse(vs));
    }

    @Override
    @Transactional
    public VehicleServiceResponse cancel(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        if (vs.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new FerosException("Only IN_PROGRESS services can be undone", HttpStatus.BAD_REQUEST);
        }
        vs.setStatus(ServiceStatus.OPEN);
        vs.setStartedAt(null);
        vehicleServiceRepository.save(vs);
        return mapToResponse(vehicleServiceRepository.findById(vs.getId()).orElse(vs));
    }

    @Override
    @Transactional
    public VehicleServiceResponse updateNotes(Long id, String notes) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        vs.setNotes(notes);
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
        if (request.getCompletedCost() != null) vs.setCompletedCost(request.getCompletedCost());

        vs.getTasks().forEach(t -> t.setStatus(ServiceTaskStatus.COMPLETED));

        // Use completedCost (3rd party bill) if provided; otherwise sum tasks + estimatedCost
        BigDecimal calculatedCost;
        if (vs.getCompletedCost() != null) {
            calculatedCost = vs.getCompletedCost();
        } else {
            calculatedCost = vs.getTasks().stream()
                    .filter(t -> t.getCost() != null)
                    .map(VehicleServiceTask::getCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(vs.getEstimatedCost() != null ? vs.getEstimatedCost() : BigDecimal.ZERO);
        }
        vs.setTotalCost(calculatedCost);

        vehicleServiceRepository.save(vs);

        // When a breakdown-triggered service is completed → resolve breakdown + move vehicle to AVAILABLE
        if (vs.getBreakdown() != null) {
            VehicleBreakdown bd = vs.getBreakdown();
            bd.setStatus(BreakdownStatus.RESOLVED);
            bd.setResolvedAt(TimeUtil.nowIst());
            vehicleBreakdownRepository.save(bd);

            vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(VehicleStatusType.AVAILABLE)
                    .ifPresent(availStatus -> {
                        vs.getVehicle().setCurrentStatus(availStatus);
                        vehicleRepository.save(vs.getVehicle());
                    });

            String serviceVehicleReg = vs.getVehicle().getRegistrationNumber();
            notificationService.sendToRoles(vs.getTenant(),
                    List.of(com.feros.api.enums.RoleName.SUPERVISOR, com.feros.api.enums.RoleName.ADMIN, com.feros.api.enums.RoleName.SERVICE_MANAGER),
                    com.feros.api.enums.NotificationType.BREAKDOWN_REPORTED,
                    "Vehicle Available — " + serviceVehicleReg,
                    serviceVehicleReg + " repair completed. Vehicle is now available for assignment.");
        }

        // Auto-schedule next services for recurring tasks (SCHEDULED flow only)
        if (vs.getTriggeredBy() == ServiceTriggeredBy.SCHEDULED && vs.getOdometer() != null) {
            autoScheduleNextServices(vs);
        }

        // Auto-create service invoice if not already created
        if (!serviceInvoiceRepository.findByServiceIdAndIsActiveTrue(id).isPresent()) {
            createServiceInvoice(vs, request);
        }

        return mapToResponse(vehicleServiceRepository.findById(vs.getId()).orElse(vs));
    }

    @Override
    @Transactional
    public VehicleServiceResponse completeTask(Long serviceId, Long taskId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        VehicleServiceTask task = vs.getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new FerosException("Task not found", HttpStatus.NOT_FOUND));

        task.setStatus(ServiceTaskStatus.COMPLETED);
        vehicleServiceTaskRepository.save(task);

        String taskName = task.getTaskType() != null ? task.getTaskType().getName()
                : (task.getCustomName() != null ? task.getCustomName() : "Task");
        String vehicleReg = vs.getVehicle().getRegistrationNumber();
        notificationService.sendToRoles(vs.getTenant(),
                List.of(com.feros.api.enums.RoleName.SERVICE_MANAGER),
                com.feros.api.enums.NotificationType.SERVICE_TASK_COMPLETE,
                "Task Done — " + vehicleReg,
                taskName + " on " + vehicleReg + " marked complete.",
                java.util.Map.of("type", "SERVICE_COMPLETE"));

        boolean allDone = vs.getTasks().stream()
                .allMatch(t -> t.getStatus() == ServiceTaskStatus.COMPLETED
                        || t.getStatus() == ServiceTaskStatus.MECHANIC_CLOSED);
        if (allDone) {
            notificationService.sendToRoles(vs.getTenant(),
                    List.of(com.feros.api.enums.RoleName.SERVICE_MANAGER),
                    com.feros.api.enums.NotificationType.SERVICE_TASK_COMPLETE,
                    "All Tasks Done — " + vehicleReg,
                    "All tasks completed on " + vehicleReg + ". Review and release the vehicle.",
                    java.util.Map.of("type", "SERVICE_COMPLETE"));
        }

        return mapToResponse(vehicleServiceRepository.findById(vs.getId()).orElse(vs));
    }

    @Override
    @Transactional
    public VehicleServiceResponse assignTask(Long serviceId, Long taskId, Long mechanicId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        VehicleServiceTask task = vs.getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new FerosException("Task not found", HttpStatus.NOT_FOUND));

        if (task.getStatus() == ServiceTaskStatus.COMPLETED) {
            throw new FerosException("Cannot reassign a completed task", HttpStatus.BAD_REQUEST);
        }

        User mechanic = userRepository.findByIdAndIsActiveTrue(mechanicId)
                .orElseThrow(() -> new FerosException("Mechanic not found", HttpStatus.NOT_FOUND));

        if (!mechanic.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Mechanic not found", HttpStatus.NOT_FOUND);
        }

        task.setAssignedMechanic(mechanic);
        task.setStatus(ServiceTaskStatus.ASSIGNED);
        vehicleServiceTaskRepository.save(task);

        // Auto-start the service when first task is assigned
        if (vs.getStatus() == ServiceStatus.OPEN) {
            vs.setStatus(ServiceStatus.IN_PROGRESS);
            vs.setStartedAt(TimeUtil.nowIst());
            vehicleServiceRepository.save(vs);
        }

        notificationService.sendToUser(vs.getTenant(), mechanic,
                com.feros.api.enums.NotificationType.TRIP_ASSIGNED,
                "Service Task Assigned — " + vs.getVehicle().getRegistrationNumber(),
                "Task: " + task.getTaskType().getName() + " | Vehicle: " + vs.getVehicle().getRegistrationNumber()
                        + " — assigned to you. Open the app to start.");

        return mapToResponse(vehicleServiceRepository.findById(vs.getId()).orElse(vs));
    }

    @Override
    @Transactional
    public VehicleServiceResponse addTask(Long serviceId, VehicleServiceTaskRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (vs.getStatus() == ServiceStatus.COMPLETED) {
            throw new FerosException("Cannot add tasks to a completed service", HttpStatus.BAD_REQUEST);
        }

        ServiceTaskType taskType = null;
        if (request.getTaskTypeId() != null) {
            taskType = serviceTaskTypeRepository.findById(request.getTaskTypeId()).orElse(null);
        }

        VehicleServiceTask task = VehicleServiceTask.builder()
                .service(vs)
                .taskType(taskType)
                .customName(request.getCustomName())
                .isRecurring(Boolean.TRUE.equals(request.getIsRecurring()))
                .frequencyKm(request.getFrequencyKm())
                .cost(request.getCost())
                .status(ServiceTaskStatus.PENDING)
                .build();
        vehicleServiceTaskRepository.save(task);

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

    @Override
    @Transactional
    public VehicleServiceResponse updateEstimatedCost(Long id, BigDecimal estimatedCost) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        vs.setEstimatedCost(estimatedCost);
        vehicleServiceRepository.save(vs);
        return mapToResponse(vs);
    }

    @Override
    @Transactional
    public VehicleServiceResponse uploadEstimateDoc(Long id, MultipartFile file) throws IOException {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        String key = s3Service.uploadFile(file, "tenants/images/services/" + id + "/estimate");
        vs.setEstimateDocUrl(key);
        vehicleServiceRepository.save(vs);
        return mapToResponse(vs);
    }

    @Override
    @Transactional
    public VehicleServiceResponse uploadBillDoc(Long id, MultipartFile file) throws IOException {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        String key = s3Service.uploadFile(file, "tenants/images/services/" + id + "/bill");
        vs.setBillDocUrl(key);
        vehicleServiceRepository.save(vs);
        return mapToResponse(vs);
    }

    private void createServiceInvoice(VehicleService vs, CompleteServiceRequest request) {
        boolean isInternal = vs.getServiceType() == VehicleServiceType.INTERNAL;

        String invoiceNumber = numberGenerator.generateFY(vs.getTenant().getId(), NumberUtil.Type.SINV);

        ServiceInvoice.ServiceInvoiceBuilder builder = ServiceInvoice.builder()
                .tenant(vs.getTenant())
                .service(vs)
                .invoiceNumber(invoiceNumber)
                .invoiceType(isInternal ? ServiceInvoiceType.INTERNAL : ServiceInvoiceType.EXTERNAL)
                .paymentStatus(ServiceInvoiceStatus.PENDING)
                .isActive(true);

        if (isInternal) {
            // Sum task costs
            BigDecimal tasksTotal = vs.getTasks().stream()
                    .map(t -> t.getCost() != null ? t.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sum approved parts costs from inventory transactions
            List<Long> servicePartIds = servicePartRepository.findByServiceIdOrderByCreatedAtDesc(vs.getId())
                    .stream()
                    .filter(p -> p.getStatus() == ServicePartStatus.APPROVED)
                    .map(ServicePart::getId)
                    .toList();
            BigDecimal partsTotal = servicePartIds.isEmpty() ? BigDecimal.ZERO :
                    sparePartsTransactionRepository.findByServicePart_IdIn(servicePartIds).stream()
                            .map(tx -> tx.getTotalCost() != null ? tx.getTotalCost() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal labour = request.getLabourCharges() != null
                    ? request.getLabourCharges() : BigDecimal.ZERO;
            BigDecimal subTotal = tasksTotal.add(partsTotal).add(labour);

            // Get GST settings — apply only if serviceInvoiceGstEnabled is true
            TenantSettings tenantSettings = tenantSettingsRepository.findByTenantId(vs.getTenant().getId()).orElse(null);
            boolean gstEnabled = tenantSettings == null || !Boolean.FALSE.equals(tenantSettings.getServiceInvoiceGstEnabled());
            BigDecimal gstRate = BigDecimal.ZERO;
            BigDecimal gstAmount = BigDecimal.ZERO;
            if (gstEnabled) {
                gstRate = tenantSettings != null && tenantSettings.getServiceGstRate() != null
                        ? tenantSettings.getServiceGstRate()
                        : BigDecimal.valueOf(18.00);
                gstAmount = subTotal.multiply(gstRate)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            BigDecimal total = subTotal.add(gstAmount);

            builder.tasksTotal(tasksTotal)
                    .partsTotal(partsTotal)
                    .labourCharges(labour)
                    .subTotal(subTotal)
                    .gstRate(gstRate)
                    .gstAmount(gstAmount)
                    .totalAmount(total);
        } else {
            BigDecimal vendorAmt = request.getVendorAmount() != null ? request.getVendorAmount() : BigDecimal.ZERO;
            builder.vendorAmount(vendorAmt)
                    .vendorInvoiceNo(request.getVendorInvoiceNo())
                    .totalAmount(vendorAmt)
                    .gstRate(BigDecimal.ZERO)
                    .gstAmount(BigDecimal.ZERO)
                    .subTotal(vendorAmt);
        }

        serviceInvoiceRepository.save(builder.build());
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

        BigDecimal tasksCost = tasks.stream()
                .filter(t -> t.getCost() != null)
                .map(VehicleServiceTask::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = tasksCost.add(
                vs.getEstimatedCost() != null ? vs.getEstimatedCost() : BigDecimal.ZERO);

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
                        .assignedMechanicId(t.getAssignedMechanic() != null ? t.getAssignedMechanic().getId() : null)
                        .assignedMechanicName(t.getAssignedMechanic() != null ? t.getAssignedMechanic().getName() : null)
                        .mechanicStartedAt(t.getMechanicStartedAt())
                        .mechanicClosedAt(t.getMechanicClosedAt())
                        .build())
                .toList();

        // Look up linked invoice if any
        ServiceInvoice invoice = serviceInvoiceRepository.findByServiceIdAndIsActiveTrue(vs.getId()).orElse(null);

        return VehicleServiceResponse.builder()
                .id(vs.getId())
                .tenantId(vs.getTenant().getId())
                .vehicleId(vs.getVehicle().getId())
                .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                .serviceNumber(vs.getServiceNumber())
                .triggeredBy(vs.getTriggeredBy())
                .breakdownId(vs.getBreakdown() != null ? vs.getBreakdown().getId() : null)
                .serviceType(vs.getServiceType())
                .payerType(vs.getPayerType())
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
                .estimatedCost(vs.getEstimatedCost())
                .completedCost(vs.getCompletedCost())
                .estimateDocUrl(vs.getEstimateDocUrl() != null ? s3Service.getPublicUrl(vs.getEstimateDocUrl()) : null)
                .billDocUrl(vs.getBillDocUrl() != null ? s3Service.getPublicUrl(vs.getBillDocUrl()) : null)
                .insuranceClaimNo(vs.getInsuranceClaimNo())
                .insuranceClaimAmt(vs.getInsuranceClaimAmt())
                .certificateNumber(vs.getCertificateNumber())
                .certificateValidUntil(vs.getCertificateValidUntil())
                .isEscalated(vs.getIsEscalated())
                .tasks(taskResponses)
                .startedAt(vs.getStartedAt())
                .createdAt(vs.getCreatedAt())
                .updatedAt(vs.getUpdatedAt())
                .invoiceId(invoice != null ? invoice.getId() : null)
                .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
                .vendorItems(serviceVendorItemRepository.findByServiceIdOrderByIdAsc(vs.getId())
                        .stream()
                        .map(i -> ServiceVendorItemResponse.builder()
                                .id(i.getId())
                                .description(i.getDescription())
                                .cost(i.getCost())
                                .build())
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public ServiceVendorItemResponse addVendorItem(Long serviceId, String description, BigDecimal cost) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        ServiceVendorItem item = ServiceVendorItem.builder()
                .service(vs)
                .tenant(tenant)
                .description(description)
                .cost(cost)
                .build();
        item = serviceVendorItemRepository.save(item);
        return ServiceVendorItemResponse.builder()
                .id(item.getId())
                .description(item.getDescription())
                .cost(item.getCost())
                .build();
    }

    @Override
    @Transactional
    public void deleteVendorItem(Long serviceId, Long itemId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        ServiceVendorItem item = serviceVendorItemRepository.findById(itemId)
                .orElseThrow(() -> new FerosException("Item not found", HttpStatus.NOT_FOUND));
        if (!item.getService().getId().equals(serviceId) || !item.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Item not found", HttpStatus.NOT_FOUND);
        }
        serviceVendorItemRepository.deleteById(itemId);
    }
}
