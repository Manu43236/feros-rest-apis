package com.feros.api.service;

import com.feros.api.entity.*;
import com.feros.api.repository.*;
import com.feros.api.service.impl.VehicleLeaseServiceImpl;
import com.feros.api.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Phase 5 gate: a night session (started 12th 21:00, closed 13th 09:00) rolls its daily log into
 *  the START date (the 12th) — the 23:59 scheduler misses it because it's still active at midnight. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LeaseDailyLogCrossingTest {

    @Mock VehicleLeaseRepository leaseRepository;
    @Mock LeaseVehicleAssignmentRepository assignmentRepository;
    @Mock LeaseVehicleSessionRepository sessionRepository;
    @Mock LeaseDailyLogRepository dailyLogRepository;
    @Mock VehicleRepository vehicleRepository;

    @InjectMocks VehicleLeaseServiceImpl service;

    @Test
    @DisplayName("ending a midnight-crossing session recomputes the daily log on the START date")
    void endSession_nightShift_dailyLogOnStartDate() {
        LocalDate startDate = LocalDate.of(2024, 1, 12);
        LocalDateTime start = startDate.atTime(21, 0);
        LocalDateTime end = startDate.plusDays(1).atTime(9, 0); // crosses midnight

        Tenant tenant = new Tenant(); tenant.setId(1L);
        VehicleLease lease = new VehicleLease(); lease.setId(100L); lease.setTenant(tenant); lease.setShiftDuty(true);
        Vehicle vehicle = new Vehicle(); vehicle.setId(7L); vehicle.setRegistrationNumber("AP01AB1234");
        LeaseVehicleAssignment assignment = new LeaseVehicleAssignment();
        assignment.setId(200L); assignment.setLease(lease); assignment.setVehicle(vehicle);

        LeaseVehicleSession session = new LeaseVehicleSession();
        session.setId(1L); session.setAssignment(assignment); session.setStartTime(start); session.setIsActive(true);

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentTenantId).thenReturn(1L);
            sec.when(SecurityUtil::getCurrentRole).thenReturn("ADMIN"); // skip driver-ownership check

            when(leaseRepository.findByIdAndTenantIdAndIsActiveTrue(100L, 1L)).thenReturn(Optional.of(lease));
            when(assignmentRepository.findByIdAndLeaseId(200L, 100L)).thenReturn(Optional.of(assignment));
            when(sessionRepository.findByAssignmentIdAndIsActiveTrue(200L)).thenReturn(Optional.of(session));
            when(sessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            // after close, the session is the only completed one started on the 12th
            when(sessionRepository.findByAssignmentIdAndIsActiveFalseAndStartTimeBetween(
                    eq(200L), eq(startDate.atStartOfDay()), eq(startDate.plusDays(1).atStartOfDay())))
                    .thenReturn(List.of(session));
            when(dailyLogRepository.findByAssignmentIdAndLogDate(200L, startDate)).thenReturn(Optional.empty());
            when(dailyLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.endSession(100L, 200L, end, new BigDecimal("50"), null);

            ArgumentCaptor<LeaseDailyLog> log = ArgumentCaptor.forClass(LeaseDailyLog.class);
            verify(dailyLogRepository).save(log.capture());
            assertThat(log.getValue().getLogDate()).isEqualTo(startDate); // the 12th, not the 13th
            assertThat(log.getValue().getSessionCount()).isEqualTo(1);
        }
    }
}
