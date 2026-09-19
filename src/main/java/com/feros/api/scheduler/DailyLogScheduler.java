package com.feros.api.scheduler;

import com.feros.api.entity.DailyLogDivision;
import com.feros.api.entity.Equipment;
import com.feros.api.entity.EquipmentDailyLog;
import com.feros.api.entity.LeaseDailyLog;
import com.feros.api.entity.LeaseVehicleAssignment;
import com.feros.api.entity.LeaseVehicleSession;
import com.feros.api.entity.MachineAssignment;
import com.feros.api.entity.MachineWorkEntry;
import com.feros.api.entity.WorkOrder;
import com.feros.api.enums.DailyLogStatus;
import com.feros.api.enums.WorkOrderStatus;
import com.feros.api.repository.DailyLogDivisionRepository;
import com.feros.api.repository.EquipmentDailyLogRepository;
import com.feros.api.repository.EquipmentRepository;
import com.feros.api.repository.LeaseDailyLogRepository;
import com.feros.api.repository.LeaseVehicleAssignmentRepository;
import com.feros.api.repository.LeaseVehicleSessionRepository;
import com.feros.api.repository.MachineAssignmentRepository;
import com.feros.api.repository.MachineWorkEntryRepository;
import com.feros.api.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyLogScheduler {

    private final WorkOrderRepository workOrderRepository;
    private final MachineAssignmentRepository machineAssignmentRepository;
    private final MachineWorkEntryRepository workEntryRepository;
    private final EquipmentDailyLogRepository dailyLogRepository;
    private final DailyLogDivisionRepository dailyLogDivisionRepository;
    private final EquipmentRepository equipmentRepository;
    private final LeaseVehicleAssignmentRepository leaseAssignmentRepository;
    private final LeaseVehicleSessionRepository leaseSessionRepository;
    private final LeaseDailyLogRepository leaseDailyLogRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void generateDailyLogsFromSessions() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[DailyLogScheduler] Running for date: {}", yesterday);

        List<WorkOrder> activeWorkOrders = workOrderRepository.findByStatusAndIsActiveTrue(WorkOrderStatus.IN_PROGRESS);

        for (WorkOrder wo : activeWorkOrders) {
            List<MachineAssignment> assignments = machineAssignmentRepository.findByWorkOrderIdOrderByStartDateAsc(wo.getId());

            for (MachineAssignment assignment : assignments) {
                if (!Boolean.TRUE.equals(assignment.getIsActive())) continue;

                // Manual takes priority — skip if any log already exists for this machine on yesterday
                if (dailyLogRepository.existsByMachineAssignmentIdAndLogDate(assignment.getId(), yesterday)) {
                    log.debug("[DailyLogScheduler] Log already exists for assignment {} on {}, skipping", assignment.getId(), yesterday);
                    continue;
                }

                List<MachineWorkEntry> sessions = workEntryRepository.findCompletedByAssignmentAndDate(assignment.getId(), yesterday);
                if (sessions.isEmpty()) continue;

                // Aggregate HMR and hours across all sessions
                BigDecimal startHmr = sessions.stream()
                        .filter(e -> e.getStartMeter() != null)
                        .map(MachineWorkEntry::getStartMeter)
                        .min(Comparator.naturalOrder()).orElse(null);
                BigDecimal endHmr = sessions.stream()
                        .filter(e -> e.getEndMeter() != null)
                        .map(MachineWorkEntry::getEndMeter)
                        .max(Comparator.naturalOrder()).orElse(null);
                BigDecimal totalHours = sessions.stream()
                        .map(e -> e.getHoursWorked() != null ? e.getHoursWorked() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP);

                // Create one log per machine per day
                EquipmentDailyLog autoLog = dailyLogRepository.save(
                        EquipmentDailyLog.builder()
                                .machineAssignment(assignment)
                                .workOrderId(wo.getId())
                                .logDate(yesterday)
                                .status(DailyLogStatus.WORKING)
                                .startHourMeter(startHmr)
                                .endHourMeter(endHmr)
                                .hoursWorked(totalHours)
                                .notes("Auto-generated from " + sessions.size() + " session(s)")
                                .source("AUTO")
                                .build());

                // Create one division line per division group, preserving order by first session startMeter
                Map<String, List<MachineWorkEntry>> byDivision = sessions.stream()
                        .collect(Collectors.groupingBy(e -> e.getDivisionName() != null ? e.getDivisionName() : ""));

                byDivision.entrySet().stream()
                        .sorted(Comparator.comparing(entry -> entry.getValue().stream()
                                .filter(e -> e.getStartMeter() != null)
                                .map(MachineWorkEntry::getStartMeter)
                                .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO)))
                        .forEach(entry -> {
                            String divisionName = entry.getKey().isEmpty() ? null : entry.getKey();
                            List<MachineWorkEntry> group = entry.getValue();

                            BigDecimal divStart = group.stream().filter(e -> e.getStartMeter() != null)
                                    .map(MachineWorkEntry::getStartMeter).min(Comparator.naturalOrder()).orElse(null);
                            BigDecimal divEnd = group.stream().filter(e -> e.getEndMeter() != null)
                                    .map(MachineWorkEntry::getEndMeter).max(Comparator.naturalOrder()).orElse(null);
                            BigDecimal divHours = group.stream()
                                    .map(e -> e.getHoursWorked() != null ? e.getHoursWorked() : BigDecimal.ZERO)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .setScale(2, RoundingMode.HALF_UP);

                            dailyLogDivisionRepository.save(DailyLogDivision.builder()
                                    .dailyLog(autoLog)
                                    .divisionName(divisionName)
                                    .startHourMeter(divStart)
                                    .endHourMeter(divEnd)
                                    .hoursWorked(divHours)
                                    .build());

                            log.info("[DailyLogScheduler] Division line: WO {} / assignment {} / division '{}' / HMR {}→{} / {}h",
                                    wo.getId(), assignment.getId(), divisionName, divStart, divEnd, divHours);
                        });

                // Sync currentMeterReading on equipment
                if (endHmr != null) {
                    Equipment eq = assignment.getEquipment();
                    if (eq.getCurrentMeterReading() == null || endHmr.compareTo(eq.getCurrentMeterReading()) > 0) {
                        eq.setCurrentMeterReading(endHmr);
                        equipmentRepository.save(eq);
                    }
                }

                log.info("[DailyLogScheduler] Created log for assignment {} on {}", assignment.getId(), yesterday);
            }
        }

        log.info("[DailyLogScheduler] Completed for date: {}", yesterday);
    }

    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void generateLeaseDailyLogs() {
        LocalDate today = LocalDate.now();
        log.info("[LeaseDailyLogScheduler] Running for date: {}", today);

        List<LeaseVehicleAssignment> assignments = leaseAssignmentRepository.findAllActiveOnActiveLeases();

        for (LeaseVehicleAssignment assignment : assignments) {
            if (leaseDailyLogRepository.existsByAssignmentIdAndLogDate(assignment.getId(), today)) {
                log.debug("[LeaseDailyLogScheduler] Log already exists for assignment {} on {}, skipping", assignment.getId(), today);
                continue;
            }

            List<LeaseVehicleSession> sessions = leaseSessionRepository
                    .findByAssignmentIdAndIsActiveFalseAndStartTimeBetween(
                            assignment.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay());

            if (sessions.isEmpty()) continue;

            BigDecimal totalHours = sessions.stream()
                    .map(s -> s.getHoursWorked() != null ? s.getHoursWorked() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal kmDriven = sessions.stream()
                    .map(s -> s.getKmDriven() != null ? s.getKmDriven() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            leaseDailyLogRepository.save(LeaseDailyLog.builder()
                    .assignment(assignment)
                    .leaseId(assignment.getLease().getId())
                    .logDate(today)
                    .totalHours(totalHours.compareTo(BigDecimal.ZERO) > 0 ? totalHours : null)
                    .kmDriven(kmDriven.compareTo(BigDecimal.ZERO) > 0 ? kmDriven : null)
                    .sessionCount(sessions.size())
                    .source("AUTO")
                    .build());

            log.info("[LeaseDailyLogScheduler] Created log for assignment {} on {}", assignment.getId(), today);
        }

        log.info("[LeaseDailyLogScheduler] Completed for date: {}", today);
    }
}
