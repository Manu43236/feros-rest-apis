package com.feros.api.scheduler;

import com.feros.api.entity.LeaseDailyLog;
import com.feros.api.entity.LeaseVehicleAssignment;
import com.feros.api.entity.LeaseVehicleSession;
import com.feros.api.enums.LeaseStatus;
import com.feros.api.repository.LeaseDailyLogRepository;
import com.feros.api.repository.LeaseVehicleAssignmentRepository;
import com.feros.api.repository.LeaseVehicleSessionRepository;
import com.feros.api.repository.VehicleLeaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaseDailyLogScheduler {

    private final VehicleLeaseRepository leaseRepository;
    private final LeaseVehicleAssignmentRepository assignmentRepository;
    private final LeaseVehicleSessionRepository sessionRepository;
    private final LeaseDailyLogRepository dailyLogRepository;

    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void generateDailyLogs() {
        LocalDate today = LocalDate.now();
        log.info("[LeaseDailyLogScheduler] Running for date: {}", today);

        leaseRepository.findByStatusAndIsActiveTrue(LeaseStatus.ACTIVE).forEach(lease -> {
            List<LeaseVehicleAssignment> assignments =
                    assignmentRepository.findByLeaseIdOrderByStartDateAsc(lease.getId());

            for (LeaseVehicleAssignment assignment : assignments) {
                if (!Boolean.TRUE.equals(assignment.getIsActive())) continue;

                if (dailyLogRepository.existsByAssignmentIdAndLogDate(assignment.getId(), today)) {
                    log.debug("[LeaseDailyLogScheduler] Log exists for assignment {} on {}, skipping",
                            assignment.getId(), today);
                    continue;
                }

                LocalDateTime dayStart = today.atStartOfDay();
                LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
                List<LeaseVehicleSession> sessions = sessionRepository
                        .findByAssignmentIdAndIsActiveFalseAndStartTimeBetween(
                                assignment.getId(), dayStart, dayEnd);

                if (sessions.isEmpty()) continue;

                BigDecimal totalHours = sessions.stream()
                        .map(s -> s.getHoursWorked() != null ? s.getHoursWorked() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                BigDecimal kmDriven = sessions.stream()
                        .map(s -> s.getKmDriven() != null ? s.getKmDriven() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

                dailyLogRepository.save(LeaseDailyLog.builder()
                        .assignment(assignment)
                        .leaseId(lease.getId())
                        .logDate(today)
                        .totalHours(totalHours.compareTo(BigDecimal.ZERO) > 0 ? totalHours : null)
                        .kmDriven(kmDriven.compareTo(BigDecimal.ZERO) > 0 ? kmDriven : null)
                        .sessionCount(sessions.size())
                        .source("AUTO")
                        .notes("Auto-generated from " + sessions.size() + " session(s)")
                        .build());

                log.info("[LeaseDailyLogScheduler] Created log for assignment {} on {}",
                        assignment.getId(), today);
            }
        });

        log.info("[LeaseDailyLogScheduler] Completed for date: {}", today);
    }
}
