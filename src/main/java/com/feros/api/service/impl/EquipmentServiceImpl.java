package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentFuelLogRequest;
import com.feros.api.dto.request.EquipmentMeterReadingRequest;
import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.request.EquipmentServiceRequest;
import com.feros.api.dto.request.EquipmentServiceTaskRequest;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.EquipmentDashboardResponse;
import com.feros.api.dto.response.EquipmentFuelLogResponse;
import com.feros.api.dto.response.EquipmentMeterReadingResponse;
import com.feros.api.dto.response.EquipmentResponse;
import com.feros.api.dto.response.EquipmentServiceResponse;
import com.feros.api.dto.response.EquipmentServiceTaskResponse;
import com.feros.api.dto.response.MachineAssignmentHistoryResponse;
import com.feros.api.dto.response.MachineInvoiceItemResponse;
import com.feros.api.entity.Equipment;
import com.feros.api.entity.EquipmentServiceRecord;
import com.feros.api.entity.EquipmentServiceTask;
import com.feros.api.entity.EquipmentDailyLog;
import com.feros.api.entity.EquipmentFuelLog;
import com.feros.api.entity.EquipmentInvoice;
import com.feros.api.entity.EquipmentMeterReading;
import com.feros.api.entity.MachineAssignment;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.WorkOrder;
import com.feros.api.entity.master.EquipmentType;
import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.EquipmentWorkStatus;
import com.feros.api.enums.ServicePayerType;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.enums.WorkOrderStatus;
import com.feros.api.repository.WorkOrderRepository;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentDailyLogRepository;
import com.feros.api.repository.EquipmentFuelLogRepository;
import com.feros.api.repository.EquipmentInvoiceItemRepository;
import com.feros.api.repository.EquipmentMeterReadingRepository;
import com.feros.api.repository.EquipmentServiceRepository;
import com.feros.api.repository.EquipmentServiceTaskTypeRepository;
import com.feros.api.repository.EquipmentRepository;
import com.feros.api.repository.EquipmentTypeRepository;
import com.feros.api.repository.MachineAssignmentRepository;
import com.feros.api.repository.SubscriptionHistoryRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.service.EquipmentService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final VehicleRepository vehicleRepository;
    private final MachineAssignmentRepository machineAssignmentRepository;
    private final EquipmentDailyLogRepository dailyLogRepository;
    private final EquipmentInvoiceItemRepository invoiceItemRepository;
    private final WorkOrderRepository workOrderRepository;
    private final EquipmentFuelLogRepository fuelLogRepository;
    private final EquipmentMeterReadingRepository meterReadingRepository;
    private final EquipmentServiceRepository equipmentServiceRepository;
    private final EquipmentServiceTaskTypeRepository equipmentServiceTaskTypeRepository;

    private Long getTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentDashboardResponse getDashboard() {
        Long tenantId = getTenantId();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        return EquipmentDashboardResponse.builder()
                .totalMachines(equipmentRepository.countByTenantId(tenantId))
                .available(equipmentRepository.countByTenantIdAndWorkStatus(tenantId, EquipmentWorkStatus.AVAILABLE))
                .assigned(equipmentRepository.countByTenantIdAndWorkStatus(tenantId, EquipmentWorkStatus.ASSIGNED))
                .busy(equipmentRepository.countByTenantIdAndWorkStatus(tenantId, EquipmentWorkStatus.BUSY))
                .inRepair(equipmentRepository.countByTenantIdAndWorkStatus(tenantId, EquipmentWorkStatus.IN_REPAIR))
                .breakdown(equipmentRepository.countByTenantIdAndWorkStatus(tenantId, EquipmentWorkStatus.BREAKDOWN))
                .activeWorkOrders(workOrderRepository.countByTenantIdAndIsActiveTrueAndStatus(tenantId, WorkOrderStatus.IN_PROGRESS))
                .totalWorkOrders(workOrderRepository.countByTenantIdAndIsActiveTrue(tenantId))
                .hoursToday(dailyLogRepository.sumHoursByTenantAndDate(tenantId, today))
                .hoursThisMonth(dailyLogRepository.sumHoursByTenantAndDateRange(tenantId, monthStart, today))
                .build();
    }

    @Override
    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findByTenantId(getTenantId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EquipmentResponse getEquipmentById(Long id) {
        return toResponse(findByIdAndTenant(id));
    }

    @Override
    @Transactional
    public EquipmentResponse createEquipment(EquipmentRequest request) {
        Long tenantId = getTenantId();
        Tenant tenant = getTenant(tenantId);

        // Slot limit check — shared pool across vehicles + machines based on moduleType
        subscriptionHistoryRepository.findActiveByTenantId(tenantId).stream().findFirst().ifPresent(h -> {
            Integer totalSlots = h.getVehicleCount();
            if (totalSlots != null && totalSlots > 0) {
                long machines = equipmentRepository.countByTenantId(tenantId);
                long combined = switch (tenant.getModuleType()) {
                    case BOTH -> vehicleRepository.countByTenantIdAndIsActiveTrue(tenantId) + machines;
                    default   -> machines; // EQUIPMENT_ONLY
                };
                if (combined >= totalSlots) {
                    throw new FerosException(
                            "Slot limit reached (" + totalSlots + "). Contact FEROS support to upgrade.",
                            HttpStatus.FORBIDDEN);
                }
            }
        });

        // Serial number uniqueness
        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            if (equipmentRepository.existsBySerialNumberAndTenantId(request.getSerialNumber(), tenantId)) {
                throw new FerosException("Serial number already exists", HttpStatus.CONFLICT);
            }
        }

        EquipmentType equipmentType = findEquipmentType(request.getEquipmentTypeId());

        Equipment equipment = Equipment.builder()
                .tenant(tenant)
                .equipmentType(equipmentType)
                .serialNumber(request.getSerialNumber())
                .registrationNumber(request.getRegistrationNumber())
                .manufactureYear(request.getManufactureYear())
                .color(request.getColor())
                .chassisNumber(request.getChassisNumber())
                .engineNumber(request.getEngineNumber())
                .fuelType(request.getFuelType())
                .fuelTankCapacity(request.getFuelTankCapacity())
                .ownershipType(request.getOwnershipType())
                .meterType(equipmentType.getDefaultMeterType())
                .currentMeterReading(request.getCurrentMeterReading())
                .workStatus(EquipmentWorkStatus.AVAILABLE)
                .isActive(true)
                .notes(request.getNotes())
                .build();

        applyOwnershipFields(equipment, request);

        return toResponse(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest request) {
        Equipment equipment = findByIdAndTenant(id);

        // Serial number uniqueness (exclude self)
        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            if (equipmentRepository.existsBySerialNumberAndTenantIdAndIdNot(
                    request.getSerialNumber(), getTenantId(), id)) {
                throw new FerosException("Serial number already exists", HttpStatus.CONFLICT);
            }
        }

        if (request.getEquipmentTypeId() != null &&
                !request.getEquipmentTypeId().equals(equipment.getEquipmentType().getId())) {
            EquipmentType newType = findEquipmentType(request.getEquipmentTypeId());
            equipment.setEquipmentType(newType);
            equipment.setMeterType(newType.getDefaultMeterType());
        }

        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setRegistrationNumber(request.getRegistrationNumber());
        equipment.setManufactureYear(request.getManufactureYear());
        equipment.setColor(request.getColor());
        equipment.setChassisNumber(request.getChassisNumber());
        equipment.setEngineNumber(request.getEngineNumber());
        equipment.setFuelType(request.getFuelType());
        equipment.setFuelTankCapacity(request.getFuelTankCapacity());
        equipment.setOwnershipType(request.getOwnershipType());
        equipment.setCurrentMeterReading(request.getCurrentMeterReading());
        equipment.setNotes(request.getNotes());
        if (request.getIsActive() != null) equipment.setIsActive(request.getIsActive());
        if (request.getWorkStatus() != null) equipment.setWorkStatus(request.getWorkStatus());

        applyOwnershipFields(equipment, request);

        return toResponse(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public EquipmentResponse updateWorkStatus(Long id, EquipmentWorkStatus workStatus) {
        Equipment equipment = findByIdAndTenant(id);
        equipment.setWorkStatus(workStatus);
        return toResponse(equipmentRepository.save(equipment));
    }

    // ── Machine Detail ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MachineAssignmentHistoryResponse> getMachineAssignmentHistory(Long equipmentId) {
        Long tenantId = getTenantId();
        findByIdAndTenant(equipmentId); // validate ownership
        List<MachineAssignment> assignments = machineAssignmentRepository.findHistoryByEquipmentId(equipmentId, tenantId);
        if (assignments.isEmpty()) return List.of();

        List<Long> assignmentIds = assignments.stream().map(MachineAssignment::getId).toList();

        // sum hoursWorked per assignment from daily logs
        Map<Long, BigDecimal> hoursByAssignment = new HashMap<>();
        dailyLogRepository.findByMachineAssignmentIdIn(assignmentIds).forEach(log -> {
            // proxy ID access — no DB query (Hibernate returns FK value directly)
            Long aid = log.getMachineAssignment().getId();
            BigDecimal h = log.getHoursWorked() != null ? log.getHoursWorked() : BigDecimal.ZERO;
            hoursByAssignment.merge(aid, h, BigDecimal::add);
        });

        return assignments.stream().map(a -> {
            WorkOrder wo = a.getWorkOrder();
            return MachineAssignmentHistoryResponse.builder()
                    .id(a.getId())
                    .workOrderId(wo.getId())
                    .woNumber(wo.getWoNumber())
                    .clientName(wo.getClient().getClientName())
                    .site(wo.getSite())
                    .workOrderStatus(wo.getStatus().name())
                    .startDate(a.getStartDate())
                    .endDate(a.getEndDate())
                    .isActive(a.getIsActive())
                    .endReason(a.getEndReason())
                    .rateType(a.getRateType())
                    .rateAmount(a.getRateAmount())
                    .totalHoursWorked(hoursByAssignment.getOrDefault(a.getId(), BigDecimal.ZERO))
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyLogResponse> getMachineDailyLogs(Long equipmentId, LocalDate from, LocalDate to) {
        Long tenantId = getTenantId();
        Equipment eq = findByIdAndTenant(equipmentId);
        List<MachineAssignment> assignments = machineAssignmentRepository.findHistoryByEquipmentId(equipmentId, tenantId);
        if (assignments.isEmpty()) return List.of();

        List<Long> assignmentIds = assignments.stream().map(MachineAssignment::getId).toList();
        // build woNumber map keyed by workOrderId (denormalized on each log)
        Map<Long, String> woNumberByWoId = assignments.stream()
                .collect(Collectors.toMap(
                        a -> a.getWorkOrder().getId(),
                        a -> a.getWorkOrder().getWoNumber(),
                        (a, b) -> a));

        List<EquipmentDailyLog> logs = (from != null && to != null)
                ? dailyLogRepository.findByMachineAssignmentIdInAndLogDateBetween(assignmentIds, from, to)
                : dailyLogRepository.findByMachineAssignmentIdIn(assignmentIds);

        String serialNumber = eq.getSerialNumber();
        String typeName = eq.getEquipmentType().getName();

        return logs.stream()
                .sorted((a, b) -> {
                    int d = b.getLogDate().compareTo(a.getLogDate());
                    return d != 0 ? d : Long.compare(b.getId(), a.getId());
                })
                .map(log -> DailyLogResponse.builder()
                        .id(log.getId())
                        .machineAssignmentId(log.getMachineAssignment().getId())
                        .workOrderId(log.getWorkOrderId())
                        .woNumber(woNumberByWoId.get(log.getWorkOrderId()))
                        .logDate(log.getLogDate())
                        .status(log.getStatus())
                        .startHourMeter(log.getStartHourMeter())
                        .endHourMeter(log.getEndHourMeter())
                        .hoursWorked(log.getHoursWorked())
                        .fuelConsumed(log.getFuelConsumed())
                        .notes(log.getNotes())
                        .source(log.getSource())
                        .serialNumber(serialNumber)
                        .equipmentTypeName(typeName)
                        .divisions(List.of()) // ponytail: not needed for machine-scope tabs
                        .createdAt(log.getCreatedAt())
                        .updatedAt(log.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineInvoiceItemResponse> getMachineInvoiceItems(Long equipmentId) {
        Long tenantId = getTenantId();
        findByIdAndTenant(equipmentId);
        return invoiceItemRepository.findByEquipmentIdAndTenantId(equipmentId, tenantId).stream()
                .map(i -> {
                    EquipmentInvoice inv = i.getInvoice();
                    return MachineInvoiceItemResponse.builder()
                            .id(i.getId())
                            .invoiceId(inv.getId())
                            .invoiceNumber(inv.getInvoiceNumber())
                            .invoiceDate(inv.getInvoiceDate())
                            .invoiceStatus(inv.getStatus().name())
                            .clientName(inv.getClient().getClientName())
                            .billingPeriodStart(inv.getBillingPeriodStart())
                            .billingPeriodEnd(inv.getBillingPeriodEnd())
                            .description(i.getDescription())
                            .billingType(i.getBillingType())
                            .quantity(i.getQuantity())
                            .rate(i.getRate())
                            .amount(i.getAmount())
                            .build();
                })
                .toList();
    }

    // ── Fuel Logs ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentFuelLogResponse> getFuelLogs(Long equipmentId) {
        Long tenantId = getTenantId();
        findByIdAndTenant(equipmentId);
        return fuelLogRepository.findByEquipmentIdAndTenantIdOrderByFillDateDescIdDesc(equipmentId, tenantId)
                .stream().map(this::toFuelLogResponse).toList();
    }

    @Override
    @Transactional
    public EquipmentFuelLogResponse addFuelLog(Long equipmentId, EquipmentFuelLogRequest req) {
        Long tenantId = getTenantId();
        Equipment eq = findByIdAndTenant(equipmentId);
        EquipmentFuelLog log = EquipmentFuelLog.builder()
                .tenant(getTenant(tenantId))
                .equipment(eq)
                .fillDate(req.getFillDate())
                .litresFilled(req.getLitresFilled())
                .hmrAtFill(req.getHmrAtFill())
                .costPerLitre(req.getCostPerLitre())
                .totalCost(req.getTotalCost())
                .isFullTank(req.getIsFullTank() != null && req.getIsFullTank())
                .paymentMode(req.getPaymentMode())
                .fuelStation(req.getFuelStation())
                .notes(req.getNotes())
                .build();
        return toFuelLogResponse(fuelLogRepository.save(log));
    }

    @Override
    @Transactional
    public EquipmentFuelLogResponse updateFuelLog(Long equipmentId, Long logId, EquipmentFuelLogRequest req) {
        findByIdAndTenant(equipmentId);
        EquipmentFuelLog log = fuelLogRepository.findByIdAndTenantId(logId, getTenantId())
                .orElseThrow(() -> new FerosException("Fuel log not found", HttpStatus.NOT_FOUND));
        log.setFillDate(req.getFillDate());
        log.setLitresFilled(req.getLitresFilled());
        log.setHmrAtFill(req.getHmrAtFill());
        log.setCostPerLitre(req.getCostPerLitre());
        log.setTotalCost(req.getTotalCost());
        if (req.getIsFullTank() != null) log.setIsFullTank(req.getIsFullTank());
        log.setPaymentMode(req.getPaymentMode());
        log.setFuelStation(req.getFuelStation());
        log.setNotes(req.getNotes());
        return toFuelLogResponse(fuelLogRepository.save(log));
    }

    @Override
    @Transactional
    public void deleteFuelLog(Long equipmentId, Long logId) {
        findByIdAndTenant(equipmentId);
        EquipmentFuelLog log = fuelLogRepository.findByIdAndTenantId(logId, getTenantId())
                .orElseThrow(() -> new FerosException("Fuel log not found", HttpStatus.NOT_FOUND));
        fuelLogRepository.delete(log);
    }

    // ── Meter Readings ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentMeterReadingResponse> getMeterReadings(Long equipmentId) {
        Long tenantId = getTenantId();
        findByIdAndTenant(equipmentId);
        return meterReadingRepository.findByEquipmentIdAndTenantIdOrderByReadingDateDescIdDesc(equipmentId, tenantId)
                .stream().map(this::toMeterReadingResponse).toList();
    }

    @Override
    @Transactional
    public EquipmentMeterReadingResponse addMeterReading(Long equipmentId, EquipmentMeterReadingRequest req) {
        Long tenantId = getTenantId();
        Equipment eq = findByIdAndTenant(equipmentId);

        // Validate: new reading must be >= max existing reading
        List<EquipmentMeterReading> existing = meterReadingRepository
                .findByEquipmentIdAndTenantIdOrderByReadingDateDescIdDesc(equipmentId, tenantId);
        if (!existing.isEmpty() && req.getReadingValue() != null) {
            BigDecimal maxExisting = existing.stream()
                    .map(EquipmentMeterReading::getReadingValue)
                    .filter(v -> v != null)
                    .max(BigDecimal::compareTo).orElse(null);
            if (maxExisting != null && req.getReadingValue().compareTo(maxExisting) < 0)
                throw new FerosException(
                    "New HMR (" + req.getReadingValue() + ") must be ≥ existing max HMR (" + maxExisting + ")",
                    HttpStatus.BAD_REQUEST);
        }

        EquipmentMeterReading reading = EquipmentMeterReading.builder()
                .tenant(getTenant(tenantId))
                .equipment(eq)
                .readingDate(req.getReadingDate())
                .readingValue(req.getReadingValue())
                .notes(req.getNotes())
                .build();
        EquipmentMeterReadingResponse result = toMeterReadingResponse(meterReadingRepository.save(reading));
        updateEquipmentMeter(eq, req.getReadingValue());
        return result;
    }

    @Override
    @Transactional
    public EquipmentMeterReadingResponse updateMeterReading(Long equipmentId, Long readingId, EquipmentMeterReadingRequest req) {
        findByIdAndTenant(equipmentId);
        EquipmentMeterReading reading = meterReadingRepository.findByIdAndTenantId(readingId, getTenantId())
                .orElseThrow(() -> new FerosException("Meter reading not found", HttpStatus.NOT_FOUND));
        Equipment eq = reading.getEquipment();

        // Validate: value must stay between neighbours (sorted by readingDate ASC)
        if (req.getReadingValue() != null) {
            List<EquipmentMeterReading> all = meterReadingRepository
                    .findByEquipmentIdAndTenantIdOrderByReadingDateDescIdDesc(eq.getId(), getTenantId())
                    .stream()
                    .sorted(java.util.Comparator.comparing(EquipmentMeterReading::getReadingDate))
                    .toList();
            int idx = -1;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId().equals(reading.getId())) { idx = i; break; }
            }
            if (idx >= 0) {
                if (idx > 0) {
                    BigDecimal prevVal = all.get(idx - 1).getReadingValue();
                    if (prevVal != null && req.getReadingValue().compareTo(prevVal) < 0)
                        throw new FerosException(
                            "HMR must be ≥ previous reading (" + prevVal + ")", HttpStatus.BAD_REQUEST);
                }
                if (idx < all.size() - 1) {
                    BigDecimal nextVal = all.get(idx + 1).getReadingValue();
                    if (nextVal != null && req.getReadingValue().compareTo(nextVal) > 0)
                        throw new FerosException(
                            "HMR must be ≤ next reading (" + nextVal + ")", HttpStatus.BAD_REQUEST);
                }
            }
        }

        reading.setReadingDate(req.getReadingDate());
        reading.setReadingValue(req.getReadingValue());
        reading.setNotes(req.getNotes());
        EquipmentMeterReadingResponse result = toMeterReadingResponse(meterReadingRepository.save(reading));
        recalculateCurrentMeter(eq);
        return result;
    }

    @Override
    @Transactional
    public void deleteMeterReading(Long equipmentId, Long readingId) {
        findByIdAndTenant(equipmentId);
        EquipmentMeterReading reading = meterReadingRepository.findByIdAndTenantId(readingId, getTenantId())
                .orElseThrow(() -> new FerosException("Meter reading not found", HttpStatus.NOT_FOUND));
        Equipment eq = reading.getEquipment();
        meterReadingRepository.delete(reading);
        recalculateCurrentMeter(eq);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Equipment findByIdAndTenant(Long id) {
        return equipmentRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new FerosException("Equipment not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentType findEquipmentType(Long typeId) {
        return equipmentTypeRepository.findById(typeId)
                .orElseThrow(() -> new FerosException("Equipment type not found", HttpStatus.NOT_FOUND));
    }

    private void updateEquipmentMeter(Equipment eq, BigDecimal newReading) {
        if (newReading == null) return;
        if (eq.getCurrentMeterReading() == null || newReading.compareTo(eq.getCurrentMeterReading()) > 0) {
            eq.setCurrentMeterReading(newReading);
            equipmentRepository.save(eq);
        }
    }

    private void recalculateCurrentMeter(Equipment eq) {
        Long tenantId = getTenantId();
        BigDecimal maxReading = meterReadingRepository
                .findByEquipmentIdAndTenantIdOrderByReadingDateDescIdDesc(eq.getId(), tenantId)
                .stream()
                .map(EquipmentMeterReading::getReadingValue)
                .filter(v -> v != null)
                .max(BigDecimal::compareTo)
                .orElse(null);
        eq.setCurrentMeterReading(maxReading);
        equipmentRepository.save(eq);
    }

    private EquipmentFuelLogResponse toFuelLogResponse(EquipmentFuelLog l) {
        return EquipmentFuelLogResponse.builder()
                .id(l.getId())
                .equipmentId(l.getEquipment().getId())
                .fillDate(l.getFillDate())
                .litresFilled(l.getLitresFilled())
                .hmrAtFill(l.getHmrAtFill())
                .costPerLitre(l.getCostPerLitre())
                .totalCost(l.getTotalCost())
                .isFullTank(l.getIsFullTank())
                .paymentMode(l.getPaymentMode())
                .fuelStation(l.getFuelStation())
                .notes(l.getNotes())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }

    private EquipmentMeterReadingResponse toMeterReadingResponse(EquipmentMeterReading r) {
        return EquipmentMeterReadingResponse.builder()
                .id(r.getId())
                .equipmentId(r.getEquipment().getId())
                .readingDate(r.getReadingDate())
                .readingValue(r.getReadingValue())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private void applyOwnershipFields(Equipment equipment, EquipmentRequest request) {
        if (request.getOwnershipType() == EquipmentOwnershipType.OWNED) {
            equipment.setIsFinanced(request.getIsFinanced() != null && request.getIsFinanced());
            equipment.setFinancerName(request.getFinancerName());
            equipment.setFinanceStartDate(request.getFinanceStartDate());
            equipment.setFinanceEndDate(request.getFinanceEndDate());
            // clear hire fields
            equipment.setHiredFrom(null);
            equipment.setHireStartDate(null);
            equipment.setHireEndDate(null);
            equipment.setHireRate(null);
            equipment.setHireRateUnit(null);
        } else {
            equipment.setHiredFrom(request.getHiredFrom());
            equipment.setHireStartDate(request.getHireStartDate());
            equipment.setHireEndDate(request.getHireEndDate());
            equipment.setHireRate(request.getHireRate());
            equipment.setHireRateUnit(request.getHireRateUnit());
            // clear finance fields
            equipment.setIsFinanced(false);
            equipment.setFinancerName(null);
            equipment.setFinanceStartDate(null);
            equipment.setFinanceEndDate(null);
        }
    }

    private EquipmentResponse toResponse(Equipment e) {
        var type = e.getEquipmentType();
        var model = type.getModel();
        var make = model.getMake();
        return EquipmentResponse.builder()
                .id(e.getId())
                .equipmentTypeId(type.getId())
                .equipmentTypeName(type.getName())
                .capacity(type.getCapacity())
                .capacityUnit(type.getCapacityUnit())
                .modelId(model.getId())
                .modelName(model.getName())
                .makeId(make.getId())
                .makeName(make.getName())
                .serialNumber(e.getSerialNumber())
                .registrationNumber(e.getRegistrationNumber())
                .manufactureYear(e.getManufactureYear())
                .color(e.getColor())
                .chassisNumber(e.getChassisNumber())
                .engineNumber(e.getEngineNumber())
                .fuelType(e.getFuelType())
                .fuelTankCapacity(e.getFuelTankCapacity())
                .ownershipType(e.getOwnershipType())
                .isFinanced(e.getIsFinanced())
                .financerName(e.getFinancerName())
                .financeStartDate(e.getFinanceStartDate())
                .financeEndDate(e.getFinanceEndDate())
                .hiredFrom(e.getHiredFrom())
                .hireStartDate(e.getHireStartDate())
                .hireEndDate(e.getHireEndDate())
                .hireRate(e.getHireRate())
                .hireRateUnit(e.getHireRateUnit())
                .meterType(e.getMeterType())
                .currentMeterReading(e.getCurrentMeterReading())
                .workStatus(e.getWorkStatus())
                .isActive(e.getIsActive())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
    // ── Service Records ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentServiceResponse> getServices(Long equipmentId) {
        Long tenantId = getTenantId();
        Equipment eq = equipmentRepository.findByIdAndTenantId(equipmentId, tenantId)
                .orElseThrow(() -> new FerosException("Equipment not found", HttpStatus.NOT_FOUND));
        BigDecimal currentHmr = eq.getCurrentMeterReading();
        return equipmentServiceRepository
                .findByEquipmentIdAndTenantIdAndIsActiveTrueOrderByCreatedAtDesc(equipmentId, tenantId)
                .stream().map(r -> toServiceResponse(r, currentHmr)).toList();
    }

    @Override
    @Transactional
    public EquipmentServiceResponse createService(Long equipmentId, EquipmentServiceRequest request) {
        Long tenantId = getTenantId();
        Tenant tenant = getTenant(tenantId);
        Equipment eq = equipmentRepository.findByIdAndTenantId(equipmentId, tenantId)
                .orElseThrow(() -> new FerosException("Equipment not found", HttpStatus.NOT_FOUND));

        EquipmentServiceRecord record = EquipmentServiceRecord.builder()
                .tenant(tenant)
                .equipment(eq)
                .serviceNumber(com.feros.api.util.NumberUtil.generate(tenant.getPrefix(), tenantId, com.feros.api.util.NumberUtil.Type.ESVC))
                .triggeredBy(request.getTriggeredBy())
                .serviceType(request.getServiceType())
                .payerType(request.getPayerType() != null ? request.getPayerType() : ServicePayerType.OWN_EXPENSE)
                .status(ServiceStatus.OPEN)
                .hmrAtService(request.getHmrAtService())
                .dueAtHmr(request.getDueAtHmr())
                .vendorName(request.getVendorName())
                .location(request.getLocation())
                .serviceDate(request.getServiceDate())
                .notes(request.getNotes())
                .insuranceClaimNo(request.getInsuranceClaimNo())
                .insuranceClaimAmt(request.getInsuranceClaimAmt())
                .certificateNumber(request.getCertificateNumber())
                .certificateValidUntil(request.getCertificateValidUntil())
                .isEscalated(Boolean.TRUE.equals(request.getIsEscalated()))
                .build();

        record = equipmentServiceRepository.save(record);
        saveTasksOnRecord(record, request.getTasks());
        record = equipmentServiceRepository.save(record);
        return toServiceResponse(record);
    }

    @Override
    @Transactional
    public EquipmentServiceResponse updateService(Long equipmentId, Long serviceId, EquipmentServiceRequest request) {
        Long tenantId = getTenantId();
        EquipmentServiceRecord record = equipmentServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (record.getStatus() == ServiceStatus.COMPLETED)
            throw new FerosException("Cannot edit a completed service record", HttpStatus.BAD_REQUEST);

        record.setTriggeredBy(request.getTriggeredBy());
        record.setServiceType(request.getServiceType());
        if (request.getPayerType() != null) record.setPayerType(request.getPayerType());
        record.setVendorName(request.getVendorName());
        record.setLocation(request.getLocation());
        record.setServiceDate(request.getServiceDate());
        record.setHmrAtService(request.getHmrAtService());
        record.setDueAtHmr(request.getDueAtHmr());
        record.setNotes(request.getNotes());
        record.setInsuranceClaimNo(request.getInsuranceClaimNo());
        record.setInsuranceClaimAmt(request.getInsuranceClaimAmt());
        record.setCertificateNumber(request.getCertificateNumber());
        record.setCertificateValidUntil(request.getCertificateValidUntil());
        record.setIsEscalated(Boolean.TRUE.equals(request.getIsEscalated()));

        record.getTasks().clear();
        saveTasksOnRecord(record, request.getTasks());
        record = equipmentServiceRepository.save(record);
        return toServiceResponse(record);
    }

    @Override
    @Transactional
    public EquipmentServiceResponse startService(Long equipmentId, Long serviceId) {
        Long tenantId = getTenantId();
        EquipmentServiceRecord record = equipmentServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (record.getStatus() != ServiceStatus.OPEN)
            throw new FerosException("Service is not in OPEN status", HttpStatus.BAD_REQUEST);

        record.setStatus(ServiceStatus.IN_PROGRESS);
        record.setStartedAt(LocalDateTime.now());

        Equipment eq = record.getEquipment();
        eq.setWorkStatus(EquipmentWorkStatus.IN_REPAIR);
        equipmentRepository.save(eq);

        return toServiceResponse(equipmentServiceRepository.save(record));
    }

    @Override
    @Transactional
    public EquipmentServiceResponse completeService(Long equipmentId, Long serviceId,
            com.feros.api.dto.request.EquipmentServiceCompleteRequest request) {
        Long tenantId = getTenantId();
        EquipmentServiceRecord record = equipmentServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (record.getStatus() == ServiceStatus.COMPLETED)
            throw new FerosException("Service is already completed", HttpStatus.BAD_REQUEST);

        BigDecimal completedHmr = request != null && request.getCompletedHmr() != null
                ? request.getCompletedHmr()
                : record.getHmrAtService();
        LocalDate completedDate = request != null && request.getCompletedDate() != null
                ? request.getCompletedDate()
                : LocalDate.now();

        // Capture recurring tasks BEFORE modifying status (avoids any post-save collection reload)
        List<EquipmentServiceTask> recurringTasks = record.getTasks().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsRecurring()) && t.getFrequencyHmr() != null)
                .toList();

        record.setStatus(ServiceStatus.COMPLETED);
        record.setCompletedDate(completedDate);
        record.setCompletedHmr(completedHmr);
        record.getTasks().forEach(t -> t.setStatus(com.feros.api.enums.ServiceTaskStatus.COMPLETED));

        // Recalculate total cost from tasks
        BigDecimal total = record.getTasks().stream()
                .filter(t -> t.getCost() != null)
                .map(EquipmentServiceTask::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        record.setTotalCost(total.compareTo(BigDecimal.ZERO) > 0 ? total : null);

        Equipment eq = record.getEquipment();
        if (eq.getWorkStatus() == EquipmentWorkStatus.IN_REPAIR) {
            boolean hasActiveAssignment = machineAssignmentRepository
                    .existsByEquipmentIdAndIsActiveTrue(eq.getId());
            eq.setWorkStatus(hasActiveAssignment ? EquipmentWorkStatus.ASSIGNED : EquipmentWorkStatus.AVAILABLE);
            equipmentRepository.save(eq);
        }

        equipmentServiceRepository.save(record);

        // Auto-create next service for recurring tasks
        if (!recurringTasks.isEmpty() && completedHmr != null) {
            BigDecimal minFreq = recurringTasks.stream()
                    .map(EquipmentServiceTask::getFrequencyHmr)
                    .min(BigDecimal::compareTo).orElse(null);
            BigDecimal nextDueHmr = minFreq != null ? completedHmr.add(minFreq) : null;

            Tenant tenant = record.getTenant();
            EquipmentServiceRecord next = EquipmentServiceRecord.builder()
                    .tenant(tenant)
                    .equipment(eq)
                    .serviceNumber(com.feros.api.util.NumberUtil.generate(tenant.getPrefix(), tenantId, com.feros.api.util.NumberUtil.Type.ESVC))
                    .triggeredBy(record.getTriggeredBy())
                    .serviceType(record.getServiceType())
                    .payerType(record.getPayerType())
                    .status(ServiceStatus.OPEN)
                    .hmrAtService(completedHmr)
                    .dueAtHmr(nextDueHmr)
                    .isActive(true)
                    .build();
            saveTasksOnRecord(next, recurringTasks.stream().map(t ->
                    com.feros.api.dto.request.EquipmentServiceTaskRequest.builder()
                            .taskTypeId(t.getTaskTypeId())
                            .customName(t.getCustomName())
                            .isRecurring(true)
                            .frequencyHmr(t.getFrequencyHmr())
                            .cost(null)
                            .build()
            ).toList());
            equipmentServiceRepository.save(next);
        }

        return toServiceResponse(record);
    }

    @Override
    @Transactional
    public void deleteService(Long equipmentId, Long serviceId) {
        Long tenantId = getTenantId();
        EquipmentServiceRecord record = equipmentServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(serviceId, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        if (record.getStatus() != ServiceStatus.OPEN)
            throw new FerosException("Only OPEN service records can be deleted", HttpStatus.BAD_REQUEST);

        record.setIsActive(false);
        equipmentServiceRepository.save(record);
    }

    private void saveTasksOnRecord(EquipmentServiceRecord record, java.util.List<EquipmentServiceTaskRequest> taskRequests) {
        if (taskRequests == null || taskRequests.isEmpty()) return;
        for (EquipmentServiceTaskRequest tr : taskRequests) {
            EquipmentServiceTask task = EquipmentServiceTask.builder()
                    .serviceRecord(record)
                    .taskTypeId(tr.getTaskTypeId())
                    .customName(tr.getCustomName())
                    .isRecurring(tr.isRecurring())
                    .frequencyHmr(tr.isRecurring() ? tr.getFrequencyHmr() : null)
                    .cost(tr.getCost())
                    .status(ServiceTaskStatus.PENDING)
                    .build();
            record.getTasks().add(task);
        }
    }

    private EquipmentServiceResponse toServiceResponse(EquipmentServiceRecord r) {
        return toServiceResponse(r, r.getEquipment().getCurrentMeterReading());
    }

    private EquipmentServiceResponse toServiceResponse(EquipmentServiceRecord r, BigDecimal currentHmr) {
        Equipment eq = r.getEquipment();
        String identifier = eq.getRegistrationNumber() != null ? eq.getRegistrationNumber() : eq.getSerialNumber();

        String displayStatus;
        if (r.getStatus() == ServiceStatus.COMPLETED) {
            displayStatus = "COMPLETED";
        } else if (r.getStatus() == ServiceStatus.IN_PROGRESS) {
            displayStatus = "IN_PROGRESS";
        } else if (r.getDueAtHmr() != null && currentHmr != null) {
            if (currentHmr.compareTo(r.getDueAtHmr()) >= 0) {
                displayStatus = "OVERDUE";
            } else if (currentHmr.compareTo(r.getDueAtHmr().subtract(new BigDecimal("50"))) >= 0) {
                displayStatus = "DUE_SOON";
            } else {
                displayStatus = "OPEN";
            }
        } else {
            displayStatus = "OPEN";
        }

        List<EquipmentServiceTaskResponse> taskResponses = r.getTasks().stream().map(t -> {
            String taskTypeName = null;
            if (t.getTaskTypeId() != null) {
                taskTypeName = equipmentServiceTaskTypeRepository.findById(t.getTaskTypeId())
                        .map(st -> st.getName()).orElse(null);
            }
            String displayName = t.getCustomName() != null ? t.getCustomName() : taskTypeName;
            return EquipmentServiceTaskResponse.builder()
                    .id(t.getId())
                    .taskTypeId(t.getTaskTypeId())
                    .taskTypeName(taskTypeName)
                    .customName(t.getCustomName())
                    .displayName(displayName)
                    .isRecurring(Boolean.TRUE.equals(t.getIsRecurring()))
                    .frequencyHmr(t.getFrequencyHmr())
                    .cost(t.getCost())
                    .status(t.getStatus())
                    .startedAt(t.getStartedAt())
                    .completedAt(t.getCompletedAt())
                    .build();
        }).toList();

        return EquipmentServiceResponse.builder()
                .id(r.getId())
                .equipmentId(eq.getId())
                .equipmentIdentifier(identifier)
                .serviceNumber(r.getServiceNumber())
                .triggeredBy(r.getTriggeredBy())
                .serviceType(r.getServiceType())
                .payerType(r.getPayerType())
                .status(r.getStatus())
                .displayStatus(displayStatus)
                .hmrAtService(r.getHmrAtService())
                .dueAtHmr(r.getDueAtHmr())
                .vendorName(r.getVendorName())
                .location(r.getLocation())
                .serviceDate(r.getServiceDate())
                .completedDate(r.getCompletedDate())
                .completedHmr(r.getCompletedHmr())
                .startedAt(r.getStartedAt())
                .totalCost(r.getTotalCost())
                .insuranceClaimNo(r.getInsuranceClaimNo())
                .insuranceClaimAmt(r.getInsuranceClaimAmt())
                .certificateNumber(r.getCertificateNumber())
                .certificateValidUntil(r.getCertificateValidUntil())
                .isEscalated(r.getIsEscalated())
                .notes(r.getNotes())
                .invoiceId(r.getInvoiceId())
                .tasks(taskResponses)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

}