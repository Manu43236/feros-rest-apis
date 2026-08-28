package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentSessionLogCloseRequest;
import com.feros.api.dto.request.EquipmentSessionLogStartRequest;
import com.feros.api.dto.response.EquipmentSessionLogResponse;
import com.feros.api.dto.response.OperatorTodayResponse;
import com.feros.api.entity.EquipmentSessionLog;
import com.feros.api.entity.MachineAssignment;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentSessionLogRepository;
import com.feros.api.repository.MachineAssignmentRepository;
import com.feros.api.service.EquipmentSessionLogService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentSessionLogServiceImpl implements EquipmentSessionLogService {

    private final EquipmentSessionLogRepository sessionLogRepo;
    private final MachineAssignmentRepository assignmentRepo;

    @Override
    public OperatorTodayResponse getToday() {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();

        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<MachineAssignment> assignments = assignmentRepo.findActiveByOperatorUserId(userId, tenantId, today);
        MachineAssignment assignment = assignments.isEmpty() ? null : assignments.get(0);

        List<EquipmentSessionLog> sessions = sessionLogRepo
                .findByOperatorUserIdAndSessionDateOrderByStartTimeDesc(userId, today);

        List<EquipmentSessionLogResponse> sessionResponses = sessions.stream()
                .map(this::toResponse)
                .toList();

        BigDecimal totalHmr = sessions.stream()
                .filter(s -> s.getEndHmr() != null && s.getStartHmr() != null)
                .map(s -> s.getEndHmr().subtract(s.getStartHmr()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OperatorTodayResponse.AssignmentSummary summary = null;
        if (assignment != null) {
            BigDecimal lastKnownHmr = sessionLogRepo
                    .findFirstByMachineAssignment_IdAndEndHmrNotNullOrderByEndTimeDesc(assignment.getId())
                    .map(EquipmentSessionLog::getEndHmr)
                    .orElse(null);

            summary = OperatorTodayResponse.AssignmentSummary.builder()
                    .id(assignment.getId())
                    .workOrderId(assignment.getWorkOrder().getId())
                    .workOrderNumber(assignment.getWorkOrder().getWoNumber())
                    .clientName(assignment.getWorkOrder().getClient().getClientName())
                    .siteName(assignment.getWorkOrder().getSite())
                    .equipmentId(assignment.getEquipment().getId())
                    .equipmentNumber(assignment.getEquipment().getRegistrationNumber())
                    .equipmentType(assignment.getEquipment().getEquipmentType().getName())
                    .lastKnownHmr(lastKnownHmr)
                    .build();
        }

        return OperatorTodayResponse.builder()
                .assignment(summary)
                .todaySessions(sessionResponses)
                .totalHmrToday(totalHmr)
                .build();
    }

    @Override
    public List<EquipmentSessionLogResponse> getMy(LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        return sessionLogRepo
                .findByOperatorUserIdAndSessionDateOrderByStartTimeAsc(userId, date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EquipmentSessionLogResponse start(EquipmentSessionLogStartRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();

        Long tenantId = SecurityUtil.getCurrentTenantId();
        MachineAssignment assignment = assignmentRepo
                .findActiveByOperatorUserId(userId, tenantId, today)
                .stream().findFirst()
                .orElseThrow(() -> new FerosException("No active machine assignment found for today", HttpStatus.BAD_REQUEST));

        EquipmentSessionLog log = EquipmentSessionLog.builder()
                .machineAssignment(assignment)
                .workOrderId(assignment.getWorkOrder().getId())
                .equipmentId(assignment.getEquipment().getId())
                .operatorUserId(userId)
                .sessionDate(today)
                .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
                .startHmr(request.getStartHmr())
                .notes(request.getNotes())
                .build();

        return toResponse(sessionLogRepo.save(log));
    }

    @Override
    public EquipmentSessionLogResponse close(Long id, EquipmentSessionLogCloseRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        EquipmentSessionLog log = sessionLogRepo.findByIdAndOperatorUserId(id, userId)
                .orElseThrow(() -> new FerosException("Session log not found", HttpStatus.NOT_FOUND));

        if (log.getEndTime() != null) {
            throw new FerosException("Session is already closed", HttpStatus.BAD_REQUEST);
        }

        if (request.getEndHmr().compareTo(log.getStartHmr()) < 0) {
            throw new FerosException("End HMR cannot be less than start HMR", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime effectiveEnd = request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now();
        if (!effectiveEnd.isAfter(log.getStartTime())) {
            throw new FerosException("End time must be after start time", HttpStatus.BAD_REQUEST);
        }

        log.setEndTime(effectiveEnd);
        log.setEndHmr(request.getEndHmr());
        log.setFuelConsumed(request.getFuelConsumed());
        if (request.getNotes() != null) log.setNotes(request.getNotes());

        return toResponse(sessionLogRepo.save(log));
    }

    @Override
    public void delete(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        EquipmentSessionLog log = sessionLogRepo.findByIdAndOperatorUserId(id, userId)
                .orElseThrow(() -> new FerosException("Session log not found", HttpStatus.NOT_FOUND));
        if (log.getEndTime() == null) {
            throw new FerosException("Cannot delete an open session — close it first", HttpStatus.BAD_REQUEST);
        }
        sessionLogRepo.delete(log);
    }

    private EquipmentSessionLogResponse toResponse(EquipmentSessionLog log) {
        BigDecimal delta = (log.getEndHmr() != null && log.getStartHmr() != null)
                ? log.getEndHmr().subtract(log.getStartHmr())
                : null;

        return EquipmentSessionLogResponse.builder()
                .id(log.getId())
                .machineAssignmentId(log.getMachineAssignment().getId())
                .workOrderId(log.getWorkOrderId())
                .equipmentId(log.getEquipmentId())
                .operatorUserId(log.getOperatorUserId())
                .sessionDate(log.getSessionDate())
                .startTime(log.getStartTime())
                .startHmr(log.getStartHmr())
                .endTime(log.getEndTime())
                .endHmr(log.getEndHmr())
                .fuelConsumed(log.getFuelConsumed())
                .hmrDelta(delta)
                .isOpen(log.getEndTime() == null)
                .notes(log.getNotes())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
