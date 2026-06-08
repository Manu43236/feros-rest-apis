package com.feros.api.scheduler;

import com.feros.api.entity.Tenant;
import com.feros.api.entity.User;
import com.feros.api.entity.master.TenantSettings;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.AttendanceRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.TenantSettingsRepository;
import com.feros.api.repository.UserRepository;
import com.feros.api.service.NotificationService;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceMissedScheduler {

    private final TenantRepository tenantRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    // Runs every minute — checks if any tenant's deadline has just passed
    @Scheduled(cron = "0 * * * * *")
    public void checkMissedAttendance() {
        LocalTime nowIst = TimeUtil.nowIst().toLocalTime().withSecond(0).withNano(0);
        LocalDate today = TimeUtil.today();

        List<Tenant> activeTenants = tenantRepository.findAllByIsActiveTrue();
        for (Tenant tenant : activeTenants) {
            TenantSettings settings = tenantSettingsRepository.findByTenantId(tenant.getId())
                    .orElse(null);
            if (settings == null) continue;
            if (!Boolean.TRUE.equals(settings.getAttendanceEnforced())) continue;

            LocalTime deadline = settings.getAttendanceDeadlineTime();
            if (deadline == null) continue;

            // Trigger only at the exact minute matching the tenant's deadline
            LocalTime deadlineMinute = deadline.withSecond(0).withNano(0);
            if (!nowIst.equals(deadlineMinute)) continue;

            log.info("Attendance deadline reached for tenant {} ({})", tenant.getId(), tenant.getCompanyName());

            // Find all DRIVER and CLEANER users for this tenant
            List<User> fieldStaff = userRepository.findByTenantIdAndRoleNames(
                    tenant.getId(), List.of(RoleName.DRIVER, RoleName.CLEANER, RoleName.SERVICE_MANAGER, RoleName.MECHANIC));

            for (User staff : fieldStaff) {
                boolean marked = attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                        staff.getId(), tenant.getId(), today);
                if (!marked) {
                    // Notify the staff member
                    notificationService.sendToUser(tenant, staff, NotificationType.ATTENDANCE,
                            "Attendance Not Marked",
                            "You have not marked your attendance for today. Please mark it immediately.");
                    log.info("Notified staff {} (id={}) for missed attendance", staff.getName(), staff.getId());
                }
            }

            // Notify all SUPERVISORs and OFFICE_STAFF
            long missedCount = fieldStaff.stream()
                    .filter(s -> !attendanceRepository.existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(
                            s.getId(), tenant.getId(), today))
                    .count();

            if (missedCount > 0) {
                notificationService.sendToRoles(tenant,
                        List.of(RoleName.SUPERVISOR, RoleName.OFFICE_STAFF),
                        NotificationType.ATTENDANCE,
                        "Missed Attendance Alert",
                        missedCount + " staff member(s) have not marked attendance for today.");
                log.info("Notified supervisors/office_staff for {} missed attendance(s) in tenant {}",
                        missedCount, tenant.getId());
            }
        }
    }
}
