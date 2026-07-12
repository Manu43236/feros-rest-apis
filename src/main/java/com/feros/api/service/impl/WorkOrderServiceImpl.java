package com.feros.api.service.impl;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.DailyLogDivisionRequest;
import com.feros.api.dto.request.DailyLogRequest;
import com.feros.api.dto.request.MachineAssignmentRequest;
import com.feros.api.dto.request.MachineConditionSurveyRequest;
import com.feros.api.dto.request.SwapMachineRequest;
import com.feros.api.dto.request.WoAmendmentRequest;
import com.feros.api.dto.request.WorkOrderRequest;
import com.feros.api.dto.response.*;
import com.feros.api.entity.*;
import com.feros.api.enums.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.WorkOrderService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final MachineAssignmentRepository machineAssignmentRepository;
    private final EquipmentDailyLogRepository dailyLogRepository;
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final EquipmentRepository equipmentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final MachineWorkEntryRepository workEntryRepository;
    private final ClientDivisionRepository clientDivisionRepository;
    private final DailyLogDivisionRepository dailyLogDivisionRepository;
    private final EquipmentAttachmentRepository attachmentRepository;
    private final WoAmendmentRepository woAmendmentRepository;
    private final MachineConditionSurveyRepository conditionSurveyRepository;

    private Long tenantId() { return SecurityUtil.getCurrentTenantId(); }

    private Tenant tenant() {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private WorkOrder fetchWo(Long id) {
        return workOrderRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenantId())
                .orElseThrow(() -> new FerosException("Work order not found", HttpStatus.NOT_FOUND));
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @Override
    public Page<WorkOrderResponse> getAll(int page, int size, WorkOrderStatus status, Long clientId) {
        return workOrderRepository
                .findAllPaged(tenantId(), status, clientId, PageRequest.of(page, size))
                .map(wo -> toResponse(wo, machineAssignmentRepository.countByWorkOrderId(wo.getId())));
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    @Override
    public WorkOrderDetailResponse getById(Long id) {
        WorkOrder wo = fetchWo(id);
        List<MachineAssignment> assignments = machineAssignmentRepository.findByWorkOrderIdOrderByStartDateAsc(id);
        List<EquipmentDailyLog> logs = dailyLogRepository.findByWorkOrderIdOrderByLogDateAscIdAsc(id);

        Map<Long, MachineAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(MachineAssignment::getId, a -> a));

        return WorkOrderDetailResponse.builder()
                .workOrder(toResponse(wo, assignments.size()))
                .assignments(assignments.stream().map(this::toAssignmentResponse).collect(Collectors.toList()))
                .logs(logs.stream().map(l -> toLogResponse(l, assignmentMap.get(l.getMachineAssignment().getId()))).collect(Collectors.toList()))
                .billing(calculateBilling(wo, assignments, logs))
                .build();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkOrderResponse create(WorkOrderRequest req) {
        Tenant t = tenant();

        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(req.getClientId(), t.getId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        WorkOrder.WorkOrderBuilder builder = WorkOrder.builder()
                .tenant(t)
                .woNumber(NumberUtil.generate(t.getPrefix(), t.getId(), NumberUtil.Type.WO))
                .client(client)
                .site(req.getSite())
                .mobilizationCharge(req.getMobilizationCharge())
                .demobilizationCharge(req.getDemobilizationCharge())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .parentWoId(req.getParentWoId())
                .notes(req.getNotes())
                .paymentTermsDays(req.getPaymentTermsDays())
                .gstPercent(req.getGstPercent())
                .retentionPercent(req.getRetentionPercent())
                .tdsPercent(req.getTdsPercent())
                .billingCycleMonths(req.getBillingCycleMonths())
                .operatorByWhom(req.getOperatorByWhom())
                .dieselByWhom(req.getDieselByWhom())
                .workingHoursPerDay(req.getWorkingHoursPerDay())
                .sundayWorking(req.getSundayWorking())
                .overtimeRateMultiplier(req.getOvertimeRateMultiplier())
                .escalationClause(req.getEscalationClause())
                .penaltyClause(req.getPenaltyClause())
                .breakdownPenaltyThresholdHours(req.getBreakdownPenaltyThresholdHours());

        WorkOrder saved = workOrderRepository.save(builder.build());
        return toResponse(saved, 0);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkOrderResponse update(Long id, WorkOrderRequest req) {
        WorkOrder wo = fetchWo(id);
        if (wo.getStatus() != WorkOrderStatus.DRAFT && wo.getStatus() != WorkOrderStatus.CONFIRMED)
            throw new FerosException("Only DRAFT or CONFIRMED work orders can be edited", HttpStatus.BAD_REQUEST);

        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(req.getClientId(), tenantId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        wo.setClient(client);
        wo.setSite(req.getSite());
        wo.setMobilizationCharge(req.getMobilizationCharge());
        wo.setDemobilizationCharge(req.getDemobilizationCharge());
        wo.setStartDate(req.getStartDate());
        wo.setEndDate(req.getEndDate());
        wo.setNotes(req.getNotes());
        wo.setPaymentTermsDays(req.getPaymentTermsDays());
        wo.setGstPercent(req.getGstPercent());
        wo.setRetentionPercent(req.getRetentionPercent());
        wo.setTdsPercent(req.getTdsPercent());
        wo.setBillingCycleMonths(req.getBillingCycleMonths());
        wo.setOperatorByWhom(req.getOperatorByWhom());
        wo.setDieselByWhom(req.getDieselByWhom());
        wo.setWorkingHoursPerDay(req.getWorkingHoursPerDay());
        wo.setSundayWorking(req.getSundayWorking());
        wo.setOvertimeRateMultiplier(req.getOvertimeRateMultiplier());
        wo.setEscalationClause(req.getEscalationClause());
        wo.setPenaltyClause(req.getPenaltyClause());
        wo.setBreakdownPenaltyThresholdHours(req.getBreakdownPenaltyThresholdHours());

        return toResponse(workOrderRepository.save(wo), machineAssignmentRepository.countByWorkOrderId(id));
    }

    // ── Status ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkOrderResponse updateStatus(Long id, WorkOrderStatus newStatus) {
        WorkOrder wo = fetchWo(id);
        validateTransition(wo.getStatus(), newStatus);
        wo.setStatus(newStatus);
        return toResponse(workOrderRepository.save(wo), machineAssignmentRepository.countByWorkOrderId(id));
    }

    private void validateTransition(WorkOrderStatus current, WorkOrderStatus next) {
        boolean valid = switch (current) {
            case DRAFT      -> next == WorkOrderStatus.CONFIRMED || next == WorkOrderStatus.CANCELLED;
            case CONFIRMED  -> next == WorkOrderStatus.IN_PROGRESS || next == WorkOrderStatus.CANCELLED;
            case IN_PROGRESS -> next == WorkOrderStatus.COMPLETED || next == WorkOrderStatus.CANCELLED;
            case COMPLETED  -> next == WorkOrderStatus.INVOICED;
            default         -> false;
        };
        if (!valid)
            throw new FerosException("Invalid status transition: " + current + " → " + next, HttpStatus.BAD_REQUEST);
    }

    // ── Extend ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkOrderResponse extend(Long id, LocalDate newEndDate) {
        WorkOrder wo = fetchWo(id);
        if (wo.getStatus() != WorkOrderStatus.IN_PROGRESS && wo.getStatus() != WorkOrderStatus.CONFIRMED)
            throw new FerosException("Only IN_PROGRESS or CONFIRMED work orders can be extended", HttpStatus.BAD_REQUEST);
        if (wo.getEndDate() != null && !newEndDate.isAfter(wo.getEndDate()))
            throw new FerosException("New end date must be after current end date", HttpStatus.BAD_REQUEST);
        wo.setEndDate(newEndDate);
        return toResponse(workOrderRepository.save(wo), machineAssignmentRepository.countByWorkOrderId(id));
    }

    // ── Machine Assignments ───────────────────────────────────────────────────

    @Override
    @Transactional
    public MachineAssignmentResponse addMachine(Long workOrderId, MachineAssignmentRequest req) {
        WorkOrder wo = fetchWo(workOrderId);
        if (wo.getStatus() == WorkOrderStatus.COMPLETED || wo.getStatus() == WorkOrderStatus.INVOICED
                || wo.getStatus() == WorkOrderStatus.CANCELLED)
            throw new FerosException("Cannot add machine to a " + wo.getStatus() + " work order", HttpStatus.BAD_REQUEST);

        Equipment equipment = equipmentRepository.findByIdAndTenantId(req.getEquipmentId(), tenantId())
                .orElseThrow(() -> new FerosException("Equipment not found", HttpStatus.NOT_FOUND));

        if (machineAssignmentRepository.existsByEquipmentIdAndIsActiveTrue(equipment.getId()))
            throw new FerosException("This machine is already assigned to another active work order", HttpStatus.CONFLICT);

        MachineAssignment assignment = machineAssignmentRepository.save(
                MachineAssignment.builder()
                        .workOrder(wo)
                        .equipment(equipment)
                        .startDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now())
                        .rateType(req.getRateType())
                        .rateAmount(req.getRateAmount())
                        .hireType(req.getHireType())
                        .guaranteedHours(req.getGuaranteedHours())
                        .overtimeRate(req.getOvertimeRate())
                        .dieselByWhom(req.getDieselByWhom())
                        .onHireDate(req.getOnHireDate())
                        .offHireDate(req.getOffHireDate())
                        .build());

        equipment.setWorkStatus(EquipmentWorkStatus.ASSIGNED);
        equipmentRepository.save(equipment);

        return toAssignmentResponse(assignment);
    }

    @Override
    @Transactional
    public void closeMachineAssignment(Long workOrderId, Long assignmentId, LocalDate endDate, AssignmentEndReason reason) {
        if (!workOrderRepository.existsByIdAndTenantId(workOrderId, tenantId()))
            throw new FerosException("Work order not found", HttpStatus.NOT_FOUND);
        MachineAssignment assignment = machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));
        assignment.setEndDate(endDate != null ? endDate : LocalDate.now());
        assignment.setEndReason(reason);
        assignment.setIsActive(false);
        machineAssignmentRepository.save(assignment);

        Equipment eq = assignment.getEquipment();
        eq.setWorkStatus(EquipmentWorkStatus.AVAILABLE);
        equipmentRepository.save(eq);
    }

    // ── Daily Logs ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DailyLogResponse addLog(Long workOrderId, DailyLogRequest req) {
        fetchWo(workOrderId);

        MachineAssignment assignment = machineAssignmentRepository
                .findByIdAndWorkOrderId(req.getMachineAssignmentId(), workOrderId)
                .orElseThrow(() -> new FerosException("Machine assignment not found for this work order", HttpStatus.NOT_FOUND));

        if (dailyLogRepository.existsByMachineAssignmentIdAndLogDate(assignment.getId(), req.getLogDate()))
            throw new FerosException("A log already exists for this machine on " + req.getLogDate(), HttpStatus.CONFLICT);

        List<DailyLogDivisionRequest> divReqs = req.getDivisions() != null ? req.getDivisions() : List.of();

        // Validate HMR continuity on first division against last saved log
        if (!divReqs.isEmpty() && divReqs.get(0).getStartHourMeter() != null) {
            dailyLogRepository.findTopByMachineAssignmentIdOrderByLogDateDescIdDesc(assignment.getId())
                    .ifPresent(last -> {
                        if (last.getEndHourMeter() != null &&
                                divReqs.get(0).getStartHourMeter().compareTo(last.getEndHourMeter()) < 0)
                            throw new FerosException(
                                "Start HM (" + divReqs.get(0).getStartHourMeter() + ") must be ≥ last log end HM (" + last.getEndHourMeter() + ")",
                                HttpStatus.BAD_REQUEST);
                    });
        }

        // Validate each division: endHMR > startHMR
        for (DailyLogDivisionRequest d : divReqs) {
            if (d.getStartHourMeter() != null && d.getEndHourMeter() != null
                    && d.getEndHourMeter().compareTo(d.getStartHourMeter()) <= 0)
                throw new FerosException("End HM must be greater than Start HM for each division", HttpStatus.BAD_REQUEST);
        }

        // Compute aggregate HMR and hours from division lines
        BigDecimal startHmr = divReqs.stream().filter(d -> d.getStartHourMeter() != null)
                .map(DailyLogDivisionRequest::getStartHourMeter)
                .min(BigDecimal::compareTo).orElse(null);
        BigDecimal endHmr = divReqs.stream().filter(d -> d.getEndHourMeter() != null)
                .map(DailyLogDivisionRequest::getEndHourMeter)
                .max(BigDecimal::compareTo).orElse(null);
        BigDecimal totalHours = divReqs.stream()
                .map(d -> calcHours(d.getStartHourMeter(), d.getEndHourMeter()))
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalHours.compareTo(BigDecimal.ZERO) == 0) totalHours = null;

        EquipmentDailyLog log = dailyLogRepository.save(
                EquipmentDailyLog.builder()
                        .machineAssignment(assignment)
                        .workOrderId(workOrderId)
                        .logDate(req.getLogDate())
                        .status(req.getStatus())
                        .startHourMeter(startHmr)
                        .endHourMeter(endHmr)
                        .hoursWorked(totalHours)
                        .fuelConsumed(req.getFuelConsumed())
                        .notes(req.getNotes())
                        .workingHours(req.getWorkingHours())
                        .idleHours(req.getIdleHours())
                        .standbyHours(req.getStandbyHours())
                        .breakdownHours(req.getBreakdownHours())
                        .idleAttribution(req.getIdleAttribution())
                        .idleReason(req.getIdleReason())
                        .signedSlipPhotoUrl(req.getSignedSlipPhotoUrl())
                        .build());

        saveDivisions(log, divReqs);
        updateEquipmentMeter(assignment.getEquipment(), endHmr);
        return toLogResponse(log, assignment);
    }

    @Override
    @Transactional
    public DailyLogResponse updateLog(Long workOrderId, Long logId, DailyLogRequest req) {
        if (!workOrderRepository.existsByIdAndTenantId(workOrderId, tenantId()))
            throw new FerosException("Work order not found", HttpStatus.NOT_FOUND);

        EquipmentDailyLog log = dailyLogRepository.findByIdAndWorkOrderId(logId, workOrderId)
                .orElseThrow(() -> new FerosException("Log not found", HttpStatus.NOT_FOUND));

        List<DailyLogDivisionRequest> divReqs = req.getDivisions() != null ? req.getDivisions() : List.of();

        // Validate each division: endHMR > startHMR
        for (DailyLogDivisionRequest d : divReqs) {
            if (d.getStartHourMeter() != null && d.getEndHourMeter() != null
                    && d.getEndHourMeter().compareTo(d.getStartHourMeter()) <= 0)
                throw new FerosException("End HM must be greater than Start HM for each division", HttpStatus.BAD_REQUEST);
        }

        BigDecimal startHmr = divReqs.stream().filter(d -> d.getStartHourMeter() != null)
                .map(DailyLogDivisionRequest::getStartHourMeter)
                .min(BigDecimal::compareTo).orElse(null);
        BigDecimal endHmr = divReqs.stream().filter(d -> d.getEndHourMeter() != null)
                .map(DailyLogDivisionRequest::getEndHourMeter)
                .max(BigDecimal::compareTo).orElse(null);
        BigDecimal totalHours = divReqs.stream()
                .map(d -> calcHours(d.getStartHourMeter(), d.getEndHourMeter()))
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalHours.compareTo(BigDecimal.ZERO) == 0) totalHours = null;

        log.setStatus(req.getStatus());
        log.setStartHourMeter(startHmr);
        log.setEndHourMeter(endHmr);
        log.setHoursWorked(totalHours);
        log.setFuelConsumed(req.getFuelConsumed());
        log.setNotes(req.getNotes());
        log.setWorkingHours(req.getWorkingHours());
        log.setIdleHours(req.getIdleHours());
        log.setStandbyHours(req.getStandbyHours());
        log.setBreakdownHours(req.getBreakdownHours());
        log.setIdleAttribution(req.getIdleAttribution());
        log.setIdleReason(req.getIdleReason());
        log.setSignedSlipPhotoUrl(req.getSignedSlipPhotoUrl());
        log.getDivisions().clear();
        dailyLogRepository.save(log);

        saveDivisions(log, divReqs);
        updateEquipmentMeter(log.getMachineAssignment().getEquipment(), endHmr);
        return toLogResponse(log, log.getMachineAssignment());
    }

    @Override
    public List<DailyLogResponse> getLogs(Long workOrderId, LocalDate from, LocalDate to) {
        fetchWo(workOrderId);
        List<EquipmentDailyLog> logs = (from != null && to != null)
                ? dailyLogRepository.findByWorkOrderIdAndLogDateBetween(workOrderId, from, to)
                : dailyLogRepository.findByWorkOrderIdOrderByLogDateAscIdAsc(workOrderId);
        return logs.stream()
                .map(l -> toLogResponse(l, l.getMachineAssignment()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLog(Long workOrderId, Long logId) {
        if (!workOrderRepository.existsByIdAndTenantId(workOrderId, tenantId()))
            throw new FerosException("Work order not found", HttpStatus.NOT_FOUND);
        EquipmentDailyLog log = dailyLogRepository.findByIdAndWorkOrderId(logId, workOrderId)
                .orElseThrow(() -> new FerosException("Log not found", HttpStatus.NOT_FOUND));
        dailyLogRepository.delete(log);
    }

    // ── Billing Calculation ───────────────────────────────────────────────────

    private BillingSummaryResponse calculateBilling(WorkOrder wo, List<MachineAssignment> assignments, List<EquipmentDailyLog> logs) {
        // Group logs by machineAssignmentId for per-assignment billing
        Map<Long, List<EquipmentDailyLog>> logsByAssignment = logs.stream()
                .filter(l -> l.getStatus() == DailyLogStatus.WORKING)
                .collect(Collectors.groupingBy(l -> l.getMachineAssignment().getId()));

        BigDecimal machineAmount = BigDecimal.ZERO;
        BigDecimal totalHours = BigDecimal.ZERO;
        int totalWorkingDays = 0;

        for (MachineAssignment a : assignments) {
            if (a.getRateType() == null || a.getRateAmount() == null) continue;
            List<EquipmentDailyLog> aLogs = logsByAssignment.getOrDefault(a.getId(), List.of());
            totalWorkingDays += aLogs.size();

            BigDecimal aHours = aLogs.stream()
                    .map(l -> l.getHoursWorked() != null ? l.getHoursWorked() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalHours = totalHours.add(aHours);

            BigDecimal aAmount = switch (a.getRateType()) {
                case HOURLY -> aHours.multiply(a.getRateAmount());
                case DAILY_SHIFT -> a.getRateAmount().multiply(BigDecimal.valueOf(aLogs.size()));
                case MONTHLY -> {
                    LocalDate from = a.getStartDate();
                    LocalDate to = a.getEndDate() != null ? a.getEndDate() : LocalDate.now();
                    long days = ChronoUnit.DAYS.between(from, to) + 1;
                    yield BigDecimal.valueOf(days).divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                            .multiply(a.getRateAmount()).setScale(2, RoundingMode.HALF_UP);
                }
            };
            machineAmount = machineAmount.add(aAmount);
        }

        BigDecimal mobilization = wo.getMobilizationCharge() != null ? wo.getMobilizationCharge() : BigDecimal.ZERO;
        BigDecimal demobilization = wo.getDemobilizationCharge() != null ? wo.getDemobilizationCharge() : BigDecimal.ZERO;
        BigDecimal total = machineAmount.add(mobilization).add(demobilization);

        return BillingSummaryResponse.builder()
                .machineRentalAmount(machineAmount.setScale(2, RoundingMode.HALF_UP))
                .operatorAmount(BigDecimal.ZERO)
                .mobilizationCharge(mobilization)
                .demobilizationCharge(demobilization)
                .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                .totalHours(totalHours.setScale(2, RoundingMode.HALF_UP))
                .totalWorkingDays(totalWorkingDays)
                .build();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private WorkOrderResponse toResponse(WorkOrder wo, long machineCount) {
        return WorkOrderResponse.builder()
                .id(wo.getId())
                .tenantId(wo.getTenant().getId())
                .woNumber(wo.getWoNumber())
                .clientId(wo.getClient().getId())
                .clientName(wo.getClient().getClientName())
                .site(wo.getSite())
                .mobilizationCharge(wo.getMobilizationCharge())
                .demobilizationCharge(wo.getDemobilizationCharge())
                .startDate(wo.getStartDate())
                .endDate(wo.getEndDate())
                .status(wo.getStatus())
                .parentWoId(wo.getParentWoId())
                .notes(wo.getNotes())
                .machineCount(machineCount)
                .createdAt(wo.getCreatedAt())
                .updatedAt(wo.getUpdatedAt())
                .paymentTermsDays(wo.getPaymentTermsDays())
                .gstPercent(wo.getGstPercent())
                .retentionPercent(wo.getRetentionPercent())
                .tdsPercent(wo.getTdsPercent())
                .billingCycleMonths(wo.getBillingCycleMonths())
                .operatorByWhom(wo.getOperatorByWhom())
                .dieselByWhom(wo.getDieselByWhom())
                .workingHoursPerDay(wo.getWorkingHoursPerDay())
                .sundayWorking(wo.getSundayWorking())
                .overtimeRateMultiplier(wo.getOvertimeRateMultiplier())
                .escalationClause(wo.getEscalationClause())
                .penaltyClause(wo.getPenaltyClause())
                .breakdownPenaltyThresholdHours(wo.getBreakdownPenaltyThresholdHours())
                .build();
    }

    @Override
    @Transactional
    public MachineAssignmentResponse setAttachment(Long workOrderId, Long assignmentId, Long attachmentId) {
        MachineAssignment assignment = machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Machine assignment not found", HttpStatus.NOT_FOUND));
        if (attachmentId == null) {
            assignment.setAttachment(null);
        } else {
            EquipmentAttachment attachment = attachmentRepository.findByIdAndTenantId(attachmentId, tenantId())
                    .orElseThrow(() -> new FerosException("Attachment not found", HttpStatus.NOT_FOUND));
            assignment.setAttachment(attachment);
        }
        return toAssignmentResponse(machineAssignmentRepository.save(assignment));
    }

    private MachineAssignmentResponse toAssignmentResponse(MachineAssignment a) {
        Equipment eq = a.getEquipment();
        return MachineAssignmentResponse.builder()
                .id(a.getId())
                .workOrderId(a.getWorkOrder().getId())
                .equipmentId(eq.getId())
                .serialNumber(eq.getSerialNumber())
                .equipmentTypeName(eq.getEquipmentType().getName())
                .makeName(eq.getEquipmentType().getModel() != null ? eq.getEquipmentType().getModel().getMake().getName() : null)
                .modelName(eq.getEquipmentType().getModel() != null ? eq.getEquipmentType().getModel().getName() : null)
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .endReason(a.getEndReason())
                .isActive(a.getIsActive())
                .operatorType(a.getOperatorType())
                .operatorStaffId(a.getOperatorStaff() != null ? a.getOperatorStaff().getId() : null)
                .operatorStaffName(a.getOperatorStaff() != null ? a.getOperatorStaff().getUser().getName() : null)
                .hiredOperatorName(a.getHiredOperatorName())
                .hiredOperatorPhone(a.getHiredOperatorPhone())
                .activeWorkEntry(workEntryRepository
                        .findByMachineAssignmentIdAndStatus(a.getId(), WorkEntryStatus.ACTIVE)
                        .map(this::toWorkEntryResponse).orElse(null))
                .divisionId(a.getDivisionId())
                .divisionName(a.getDivisionName())
                .rateType(a.getRateType())
                .rateAmount(a.getRateAmount())
                .attachmentId(a.getAttachment() != null ? a.getAttachment().getId() : null)
                .attachmentName(a.getAttachment() != null ? a.getAttachment().getName() : null)
                .attachmentType(a.getAttachment() != null ? a.getAttachment().getType() : null)
                .attachmentRate(a.getAttachment() != null ? a.getAttachment().getDefaultRate() : null)
                .attachmentRateUnit(a.getAttachment() != null ? a.getAttachment().getRateUnit() : null)
                .hireType(a.getHireType())
                .guaranteedHours(a.getGuaranteedHours())
                .overtimeRate(a.getOvertimeRate())
                .dieselByWhom(a.getDieselByWhom())
                .onHireDate(a.getOnHireDate())
                .offHireDate(a.getOffHireDate())
                .swappedFromAssignmentId(a.getSwappedFromAssignmentId())
                .build();
    }

    public MachineAssignmentResponse assignOperator(Long workOrderId, Long assignmentId, com.feros.api.dto.request.AssignOperatorRequest req) {
        MachineAssignment assignment = machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Machine assignment not found", HttpStatus.NOT_FOUND));

        assignment.setOperatorType(req.getOperatorType());
        if (req.getOperatorType() == null) {
            assignment.setOperatorStaff(null);
            assignment.setHiredOperatorName(null);
            assignment.setHiredOperatorPhone(null);
        } else if (req.getOperatorType() == com.feros.api.enums.OperatorType.OWN_STAFF) {
            StaffProfile staff = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(req.getOperatorStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Staff not found", HttpStatus.NOT_FOUND));
            assignment.setOperatorStaff(staff);
            assignment.setHiredOperatorName(null);
            assignment.setHiredOperatorPhone(null);
        } else {
            assignment.setOperatorStaff(null);
            assignment.setHiredOperatorName(req.getHiredOperatorName());
            assignment.setHiredOperatorPhone(req.getHiredOperatorPhone());
        }
        return toAssignmentResponse(machineAssignmentRepository.save(assignment));
    }

    public MachineAssignmentResponse assignDivision(Long workOrderId, Long assignmentId, AssignDivisionRequest req) {
        MachineAssignment assignment = machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Machine assignment not found", HttpStatus.NOT_FOUND));

        if (req.getDivisionId() == null) {
            assignment.setDivisionId(null);
            assignment.setDivisionName(null);
        } else {
            ClientDivision division = clientDivisionRepository.findById(req.getDivisionId())
                    .orElseThrow(() -> new FerosException("Division not found", HttpStatus.NOT_FOUND));
            assignment.setDivisionId(division.getId());
            assignment.setDivisionName(division.getName());
        }
        return toAssignmentResponse(machineAssignmentRepository.save(assignment));
    }

    public WorkEntryResponse startWork(Long workOrderId, Long assignmentId, com.feros.api.dto.request.StartWorkEntryRequest req) {
        MachineAssignment assignment = machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Machine assignment not found", HttpStatus.NOT_FOUND));

        if (workEntryRepository.existsByMachineAssignmentIdAndStatus(assignmentId, WorkEntryStatus.ACTIVE))
            throw new FerosException("Machine is already running. Stop the current session first.", HttpStatus.CONFLICT);

        // Meter continuity: start meter must be ≥ last completed session's end meter
        workEntryRepository
                .findTopByMachineAssignmentIdAndStatusOrderByEndTimeDesc(assignmentId, WorkEntryStatus.COMPLETED)
                .ifPresent(last -> {
                    if (last.getEndMeter() != null && req.getStartMeter() != null
                            && req.getStartMeter().compareTo(last.getEndMeter()) < 0) {
                        throw new FerosException(
                                "Start meter (" + req.getStartMeter() + ") must be ≥ last session end meter (" + last.getEndMeter() + ")",
                                HttpStatus.BAD_REQUEST);
                    }
                });

        MachineWorkEntry.MachineWorkEntryBuilder builder = MachineWorkEntry.builder()
                .machineAssignment(assignment)
                .operatorType(req.getOperatorType())
                .startTime(java.time.LocalDateTime.now())
                .startMeter(req.getStartMeter())
                .divisionName(assignment.getDivisionName()); // snapshot at start time

        if (req.getOperatorType() == OperatorType.OWN_STAFF) {
            StaffProfile staff = staffProfileRepository.findByUserIdAndTenantIdAndIsActiveTrue(req.getOperatorStaffId(), tenantId())
                    .orElseThrow(() -> new FerosException("Staff not found", HttpStatus.NOT_FOUND));
            builder.operatorStaff(staff);
        } else {
            builder.hiredOperatorName(req.getHiredOperatorName());
        }

        WorkEntryResponse response = toWorkEntryResponse(workEntryRepository.save(builder.build()));
        Equipment eq = assignment.getEquipment();
        eq.setWorkStatus(EquipmentWorkStatus.BUSY);
        equipmentRepository.save(eq);
        return response;
    }

    public WorkEntryResponse stopWork(Long workOrderId, Long assignmentId, com.feros.api.dto.request.StopWorkEntryRequest req) {
        machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Machine assignment not found", HttpStatus.NOT_FOUND));

        MachineWorkEntry entry = workEntryRepository.findByMachineAssignmentIdAndStatus(assignmentId, WorkEntryStatus.ACTIVE)
                .orElseThrow(() -> new FerosException("No active session found for this machine", HttpStatus.NOT_FOUND));

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        entry.setEndTime(now);
        entry.setEndMeter(req.getEndMeter());
        entry.setNotes(req.getNotes());
        entry.setStatus(WorkEntryStatus.COMPLETED);

        if (entry.getStartMeter() != null && req.getEndMeter() != null)
            entry.setHoursWorked(req.getEndMeter().subtract(entry.getStartMeter()).setScale(2, RoundingMode.HALF_UP));

        WorkEntryResponse response = toWorkEntryResponse(workEntryRepository.save(entry));
        Equipment eq = entry.getMachineAssignment().getEquipment();
        eq.setWorkStatus(EquipmentWorkStatus.ASSIGNED);
        equipmentRepository.save(eq);
        updateEquipmentMeter(eq, req.getEndMeter());
        return response;
    }

    public List<WorkEntryResponse> getWorkEntries(Long workOrderId, Long assignmentId) {
        machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Machine assignment not found", HttpStatus.NOT_FOUND));
        return workEntryRepository.findByMachineAssignmentIdOrderByStartTimeDesc(assignmentId)
                .stream().map(this::toWorkEntryResponse).collect(Collectors.toList());
    }

    public List<WorkEntryResponse> getAllWorkEntries(Long workOrderId, LocalDate from, LocalDate to) {
        fetchWo(workOrderId);
        List<MachineWorkEntry> entries = (from != null && to != null)
                ? workEntryRepository.findByMachineAssignment_WorkOrder_IdAndStartTimeBetweenOrderByStartTimeDesc(
                        workOrderId, from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                : workEntryRepository.findByMachineAssignment_WorkOrder_IdOrderByStartTimeDesc(workOrderId);
        return entries.stream().map(this::toWorkEntryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int convertWorkEntriesToLogs(Long workOrderId) {
        WorkOrder wo = fetchWo(workOrderId);

        // All completed work entries for this WO grouped by assignment → date
        Map<MachineAssignment, Map<LocalDate, List<MachineWorkEntry>>> grouped =
                workEntryRepository.findByMachineAssignment_WorkOrder_IdOrderByStartTimeDesc(workOrderId)
                        .stream()
                        .filter(e -> e.getStatus() == WorkEntryStatus.COMPLETED)
                        .collect(Collectors.groupingBy(
                                MachineWorkEntry::getMachineAssignment,
                                Collectors.groupingBy(e -> e.getStartTime().toLocalDate())
                        ));

        int created = 0;
        for (Map.Entry<MachineAssignment, Map<LocalDate, List<MachineWorkEntry>>> assignEntry : grouped.entrySet()) {
            MachineAssignment assignment = assignEntry.getKey();
            for (Map.Entry<LocalDate, List<MachineWorkEntry>> dateEntry : assignEntry.getValue().entrySet()) {
                LocalDate date = dateEntry.getKey();
                List<MachineWorkEntry> sessions = dateEntry.getValue();

                // Skip if a log already exists for this machine on this date
                if (dailyLogRepository.existsByMachineAssignmentIdAndLogDate(assignment.getId(), date)) continue;

                BigDecimal startHmr = sessions.stream()
                        .filter(e -> e.getStartMeter() != null).map(MachineWorkEntry::getStartMeter)
                        .min(java.util.Comparator.naturalOrder()).orElse(null);
                BigDecimal endHmr = sessions.stream()
                        .filter(e -> e.getEndMeter() != null).map(MachineWorkEntry::getEndMeter)
                        .max(java.util.Comparator.naturalOrder()).orElse(null);
                BigDecimal totalHours = sessions.stream()
                        .map(e -> e.getHoursWorked() != null ? e.getHoursWorked() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

                EquipmentDailyLog log = dailyLogRepository.save(
                        EquipmentDailyLog.builder()
                                .machineAssignment(assignment)
                                .workOrderId(wo.getId())
                                .logDate(date)
                                .status(DailyLogStatus.WORKING)
                                .startHourMeter(startHmr)
                                .endHourMeter(endHmr)
                                .hoursWorked(totalHours)
                                .notes("Converted from " + sessions.size() + " session(s)")
                                .source("AUTO")
                                .build());

                // Division lines grouped by division snapshot
                Map<String, List<MachineWorkEntry>> byDivision = sessions.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getDivisionName() != null ? e.getDivisionName() : ""));
                byDivision.forEach((div, group) -> {
                    BigDecimal divStart = group.stream().filter(e -> e.getStartMeter() != null)
                            .map(MachineWorkEntry::getStartMeter).min(java.util.Comparator.naturalOrder()).orElse(null);
                    BigDecimal divEnd = group.stream().filter(e -> e.getEndMeter() != null)
                            .map(MachineWorkEntry::getEndMeter).max(java.util.Comparator.naturalOrder()).orElse(null);
                    BigDecimal divHours = group.stream()
                            .map(e -> e.getHoursWorked() != null ? e.getHoursWorked() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                    dailyLogDivisionRepository.save(DailyLogDivision.builder()
                            .dailyLog(log)
                            .divisionName(div.isEmpty() ? null : div)
                            .startHourMeter(divStart)
                            .endHourMeter(divEnd)
                            .hoursWorked(divHours)
                            .build());
                });
                created++;
            }
        }
        return created;
    }

    private WorkEntryResponse toWorkEntryResponse(MachineWorkEntry e) {
        MachineAssignment a = e.getMachineAssignment();
        Equipment eq = a.getEquipment();
        return WorkEntryResponse.builder()
                .id(e.getId())
                .machineAssignmentId(a.getId())
                .serialNumber(eq.getSerialNumber())
                .equipmentTypeName(eq.getEquipmentType().getName())
                .divisionName(e.getDivisionName()) // use snapshot, not current assignment state
                .status(e.getStatus())
                .operatorType(e.getOperatorType())
                .operatorStaffId(e.getOperatorStaff() != null ? e.getOperatorStaff().getId() : null)
                .operatorStaffName(e.getOperatorStaff() != null ? e.getOperatorStaff().getUser().getName() : null)
                .hiredOperatorName(e.getHiredOperatorName())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .startMeter(e.getStartMeter())
                .endMeter(e.getEndMeter())
                .hoursWorked(e.getHoursWorked())
                .notes(e.getNotes())
                .build();
    }

    private void saveDivisions(EquipmentDailyLog log, List<DailyLogDivisionRequest> divReqs) {
        for (DailyLogDivisionRequest d : divReqs) {
            String divName = null;
            if (d.getDivisionId() != null)
                divName = clientDivisionRepository.findById(d.getDivisionId()).map(cd -> cd.getName()).orElse(null);

            BigDecimal hours = calcHours(d.getStartHourMeter(), d.getEndHourMeter());

            dailyLogDivisionRepository.save(DailyLogDivision.builder()
                    .dailyLog(log)
                    .divisionName(divName)
                    .startHourMeter(d.getStartHourMeter())
                    .endHourMeter(d.getEndHourMeter())
                    .hoursWorked(hours)
                    .notes(d.getNotes())
                    .build());
        }
    }

    private DailyLogResponse toLogResponse(EquipmentDailyLog l, MachineAssignment assignment) {
        Equipment eq = assignment != null ? assignment.getEquipment() : null;
        List<DailyLogDivisionResponse> divResponses = dailyLogDivisionRepository.findByDailyLogId(l.getId())
                .stream()
                .map(d -> DailyLogDivisionResponse.builder()
                        .id(d.getId())
                        .divisionName(d.getDivisionName())
                        .startHourMeter(d.getStartHourMeter())
                        .endHourMeter(d.getEndHourMeter())
                        .hoursWorked(d.getHoursWorked())
                        .notes(d.getNotes())
                        .build())
                .collect(Collectors.toList());

        return DailyLogResponse.builder()
                .id(l.getId())
                .machineAssignmentId(l.getMachineAssignment().getId())
                .workOrderId(l.getWorkOrderId())
                .logDate(l.getLogDate())
                .status(l.getStatus())
                .startHourMeter(l.getStartHourMeter())
                .endHourMeter(l.getEndHourMeter())
                .hoursWorked(l.getHoursWorked())
                .fuelConsumed(l.getFuelConsumed())
                .notes(l.getNotes())
                .source(l.getSource())
                .serialNumber(eq != null ? eq.getSerialNumber() : null)
                .equipmentTypeName(eq != null ? eq.getEquipmentType().getName() : null)
                .divisions(divResponses)
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .workingHours(l.getWorkingHours())
                .idleHours(l.getIdleHours())
                .standbyHours(l.getStandbyHours())
                .breakdownHours(l.getBreakdownHours())
                .idleAttribution(l.getIdleAttribution())
                .idleReason(l.getIdleReason())
                .signedSlipPhotoUrl(l.getSignedSlipPhotoUrl())
                .build();
    }

    private void updateEquipmentMeter(Equipment eq, BigDecimal newReading) {
        if (newReading == null) return;
        if (eq.getCurrentMeterReading() == null || newReading.compareTo(eq.getCurrentMeterReading()) > 0) {
            eq.setCurrentMeterReading(newReading);
            equipmentRepository.save(eq);
        }
    }

    private BigDecimal calcHours(BigDecimal start, BigDecimal end) {
        if (start == null || end == null) return null;
        BigDecimal diff = end.subtract(start);
        return diff.compareTo(BigDecimal.ZERO) >= 0 ? diff.setScale(2, RoundingMode.HALF_UP) : null;
    }

    // ── KAN-19 Amendments ─────────────────────────────────────────────────────

    @Override
    public List<WoAmendmentResponse> getAmendments(Long workOrderId) {
        fetchWo(workOrderId);
        return woAmendmentRepository.findByWorkOrderIdOrderByEffectiveDateDesc(workOrderId)
                .stream().map(this::toAmendmentResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WoAmendmentResponse createAmendment(Long workOrderId, WoAmendmentRequest req, String createdBy) {
        WorkOrder wo = fetchWo(workOrderId);
        WoAmendment amendment = woAmendmentRepository.save(WoAmendment.builder()
                .workOrder(wo)
                .amendmentType(req.getAmendmentType())
                .effectiveDate(req.getEffectiveDate())
                .oldValue(req.getOldValue())
                .newValue(req.getNewValue())
                .reason(req.getReason())
                .createdBy(createdBy)
                .build());
        return toAmendmentResponse(amendment);
    }

    private WoAmendmentResponse toAmendmentResponse(WoAmendment a) {
        return WoAmendmentResponse.builder()
                .id(a.getId())
                .workOrderId(a.getWorkOrder().getId())
                .amendmentType(a.getAmendmentType())
                .effectiveDate(a.getEffectiveDate())
                .oldValue(a.getOldValue())
                .newValue(a.getNewValue())
                .reason(a.getReason())
                .createdBy(a.getCreatedBy())
                .createdAt(a.getCreatedAt())
                .build();
    }

    // ── KAN-20 Machine Swap ───────────────────────────────────────────────────

    @Override
    @Transactional
    public MachineAssignmentResponse swapMachine(Long workOrderId, Long assignmentId, SwapMachineRequest req) {
        WorkOrder wo = fetchWo(workOrderId);
        MachineAssignment old = machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));
        if (!old.getIsActive())
            throw new FerosException("Assignment is already closed", HttpStatus.BAD_REQUEST);

        Equipment newEquipment = equipmentRepository.findByIdAndTenantId(req.getNewEquipmentId(), tenantId())
                .orElseThrow(() -> new FerosException("Equipment not found", HttpStatus.NOT_FOUND));
        if (machineAssignmentRepository.existsByEquipmentIdAndIsActiveTrue(newEquipment.getId()))
            throw new FerosException("Replacement machine is already assigned to another work order", HttpStatus.CONFLICT);

        // Close old assignment
        old.setEndDate(req.getEffectiveDate());
        old.setEndReason(AssignmentEndReason.BREAKDOWN_REPLACED);
        old.setIsActive(false);
        machineAssignmentRepository.save(old);
        old.getEquipment().setWorkStatus(EquipmentWorkStatus.AVAILABLE);
        equipmentRepository.save(old.getEquipment());

        // Create new assignment carrying over terms from old
        MachineAssignment replacement = machineAssignmentRepository.save(MachineAssignment.builder()
                .workOrder(wo)
                .equipment(newEquipment)
                .startDate(req.getEffectiveDate())
                .rateType(old.getRateType())
                .rateAmount(old.getRateAmount())
                .hireType(old.getHireType())
                .guaranteedHours(old.getGuaranteedHours())
                .overtimeRate(old.getOvertimeRate())
                .dieselByWhom(old.getDieselByWhom())
                .onHireDate(req.getEffectiveDate())
                .divisionId(old.getDivisionId())
                .divisionName(old.getDivisionName())
                .swappedFromAssignmentId(old.getId())
                .build());

        newEquipment.setWorkStatus(EquipmentWorkStatus.ASSIGNED);
        equipmentRepository.save(newEquipment);

        // Log amendment
        woAmendmentRepository.save(WoAmendment.builder()
                .workOrder(wo)
                .amendmentType(AmendmentType.MACHINE_SWAP)
                .effectiveDate(req.getEffectiveDate())
                .oldValue("assignmentId=" + old.getId() + ",equipmentId=" + old.getEquipment().getId())
                .newValue("assignmentId=" + replacement.getId() + ",equipmentId=" + newEquipment.getId())
                .reason(req.getReason())
                .createdBy(String.valueOf(SecurityUtil.getCurrentUserId()))
                .build());

        return toAssignmentResponse(replacement);
    }

    // ── KAN-21 Condition Surveys ──────────────────────────────────────────────

    @Override
    public List<MachineConditionSurveyResponse> getSurveys(Long workOrderId, Long assignmentId) {
        machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));
        return conditionSurveyRepository
                .findByMachineAssignmentIdAndIsActiveTrueOrderBySurveyDateDesc(assignmentId)
                .stream().map(this::toSurveyResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MachineConditionSurveyResponse createSurvey(Long workOrderId, Long assignmentId, MachineConditionSurveyRequest req) {
        MachineAssignment assignment = machineAssignmentRepository.findByIdAndWorkOrderId(assignmentId, workOrderId)
                .orElseThrow(() -> new FerosException("Assignment not found", HttpStatus.NOT_FOUND));

        String photosJson = req.getPhotos() != null && !req.getPhotos().isEmpty()
                ? "[" + req.getPhotos().stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(",")) + "]"
                : null;

        MachineConditionSurvey survey = conditionSurveyRepository.save(MachineConditionSurvey.builder()
                .machineAssignment(assignment)
                .surveyType(req.getSurveyType())
                .surveyDate(req.getSurveyDate())
                .hmrAtSurvey(req.getHmrAtSurvey())
                .conditionNotes(req.getConditionNotes())
                .photos(photosJson)
                .surveyedBy(req.getSurveyedBy())
                .build());
        return toSurveyResponse(survey);
    }

    private MachineConditionSurveyResponse toSurveyResponse(MachineConditionSurvey s) {
        List<String> photos = null;
        if (s.getPhotos() != null && !s.getPhotos().isBlank()) {
            // Parse simple JSON array of strings produced by createSurvey
            String raw = s.getPhotos().trim().replaceAll("^\\[|]$", "");
            photos = raw.isBlank() ? List.of()
                    : java.util.Arrays.stream(raw.split(","))
                            .map(p -> p.trim().replaceAll("^\"|\"$", ""))
                            .collect(Collectors.toList());
        }
        return MachineConditionSurveyResponse.builder()
                .id(s.getId())
                .machineAssignmentId(s.getMachineAssignment().getId())
                .surveyType(s.getSurveyType())
                .surveyDate(s.getSurveyDate())
                .hmrAtSurvey(s.getHmrAtSurvey())
                .conditionNotes(s.getConditionNotes())
                .photos(photos)
                .surveyedBy(s.getSurveyedBy())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
