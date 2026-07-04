package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.EquipmentResponse;
import com.feros.api.dto.response.MachineAssignmentHistoryResponse;
import com.feros.api.dto.response.MachineInvoiceItemResponse;
import com.feros.api.entity.Equipment;
import com.feros.api.entity.EquipmentDailyLog;
import com.feros.api.entity.EquipmentInvoice;
import com.feros.api.entity.MachineAssignment;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.WorkOrder;
import com.feros.api.entity.master.EquipmentType;
import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.EquipmentWorkStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentDailyLogRepository;
import com.feros.api.repository.EquipmentInvoiceItemRepository;
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

    private Long getTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Equipment findByIdAndTenant(Long id) {
        return equipmentRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new FerosException("Equipment not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentType findEquipmentType(Long typeId) {
        return equipmentTypeRepository.findById(typeId)
                .orElseThrow(() -> new FerosException("Equipment type not found", HttpStatus.NOT_FOUND));
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
}
