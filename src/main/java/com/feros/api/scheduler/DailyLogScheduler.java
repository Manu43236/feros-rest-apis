package com.feros.api.scheduler;

import com.feros.api.entity.EquipmentDailyLog;
import com.feros.api.entity.MachineAssignment;
import com.feros.api.entity.MachineWorkEntry;
import com.feros.api.entity.WorkOrder;
import com.feros.api.enums.DailyLogStatus;
import com.feros.api.enums.WorkOrderStatus;
import com.feros.api.repository.EquipmentDailyLogRepository;
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

                // Skip if a log already exists for this machine on yesterday (manual takes priority)
                if (dailyLogRepository.existsByMachineAssignmentIdAndLogDate(assignment.getId(), yesterday)) {
                    log.debug("[DailyLogScheduler] Log already exists for assignment {} on {}, skipping", assignment.getId(), yesterday);
                    continue;
                }

                List<MachineWorkEntry> sessions = workEntryRepository.findCompletedByAssignmentAndDate(assignment.getId(), yesterday);
                if (sessions.isEmpty()) continue;

                // Group by divisionName (null = no division)
                Map<String, List<MachineWorkEntry>> byDivision = sessions.stream()
                        .collect(Collectors.groupingBy(e -> e.getDivisionName() != null ? e.getDivisionName() : ""));

                for (Map.Entry<String, List<MachineWorkEntry>> entry : byDivision.entrySet()) {
                    String divisionName = entry.getKey().isEmpty() ? null : entry.getKey();
                    List<MachineWorkEntry> group = entry.getValue();

                    BigDecimal totalHours = group.stream()
                            .map(e -> e.getHoursWorked() != null ? e.getHoursWorked() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal startHmr = group.stream()
                            .filter(e -> e.getStartMeter() != null)
                            .min(Comparator.comparing(MachineWorkEntry::getStartMeter))
                            .map(MachineWorkEntry::getStartMeter)
                            .orElse(null);

                    BigDecimal endHmr = group.stream()
                            .filter(e -> e.getEndMeter() != null)
                            .max(Comparator.comparing(MachineWorkEntry::getEndMeter))
                            .map(MachineWorkEntry::getEndMeter)
                            .orElse(null);

                    EquipmentDailyLog autoLog = EquipmentDailyLog.builder()
                            .machineAssignment(assignment)
                            .workOrderId(wo.getId())
                            .logDate(yesterday)
                            .status(DailyLogStatus.WORKING)
                            .startHourMeter(startHmr)
                            .endHourMeter(endHmr)
                            .hoursWorked(totalHours)
                            .divisionName(divisionName)
                            .notes("Auto-generated from " + group.size() + " session(s)")
                            .source("AUTO")
                            .build();

                    dailyLogRepository.save(autoLog);
                    log.info("[DailyLogScheduler] Created daily log for WO {} / assignment {} / division '{}' / date {}",
                            wo.getId(), assignment.getId(), divisionName, yesterday);
                }
            }
        }

        log.info("[DailyLogScheduler] Completed for date: {}", yesterday);
    }
}
