package com.feros.api.scheduler;

import com.feros.api.entity.Attendance;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.Tyre;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.enums.TyreStatus;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.repository.AttendanceRepository;
import com.feros.api.repository.OrderStaffAllocationRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.TyreRepository;
import com.feros.api.repository.VehicleDocumentRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.service.NotificationService;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailySummaryScheduler {

    private final TenantRepository tenantRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final TyreRepository tyreRepository;
    private final VehicleRepository vehicleRepository;
    private final AttendanceRepository attendanceRepository;
    private final OrderStaffAllocationRepository staffAllocationRepository;
    private final NotificationService notificationService;

    private static final List<RoleName> OFFICE_ROLES = List.of(RoleName.OFFICE_STAFF, RoleName.ADMIN);

    /** 9 AM: expired docs + tyre alerts */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void morningAlerts() {
        LocalDate today = TimeUtil.today();
        for (Tenant tenant : tenantRepository.findAll()) {
            sendExpiredDocsAlert(tenant, today);
            sendTyreStatusAlert(tenant);
        }
    }

    /** 10 AM: fleet status summary */
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional(readOnly = true)
    public void fleetStatusSummary() {
        for (Tenant tenant : tenantRepository.findAll()) {
            sendFleetStatusAlert(tenant);
        }
    }

    /** 6 PM: attendance summary + driver vehicle summary */
    @Scheduled(cron = "0 0 18 * * *")
    @Transactional(readOnly = true)
    public void eveningAlerts() {
        LocalDate today = TimeUtil.today();
        for (Tenant tenant : tenantRepository.findAll()) {
            sendAttendanceSummary(tenant, today);
            sendDriverVehicleSummary(tenant, today);
        }
    }

    private void sendExpiredDocsAlert(Tenant tenant, LocalDate today) {
        long count = vehicleDocumentRepository.findExpiringDocuments(tenant.getId(), today).size();
        if (count == 0) return;
        notificationService.sendToRoles(tenant, OFFICE_ROLES,
                NotificationType.DAILY_SUMMARY,
                "Expired Vehicle Documents — Update Required",
                count + " vehicle document(s) have expired. Please update them to stay compliant.",
                Map.of("type", "DAILY_SUMMARY"));
    }

    private void sendTyreStatusAlert(Tenant tenant) {
        long retreading = tyreRepository
                .findByTenantIdAndStatusAndIsActiveTrueOrderByIdDesc(tenant.getId(), TyreStatus.RETREADING)
                .size();
        long nearEndOfLife = tyreRepository
                .findByTenantIdAndStatusAndIsActiveTrueOrderByIdDesc(tenant.getId(), TyreStatus.FITTED)
                .stream()
                .filter(t -> t.getMaxLifetimeKm() != null
                        && t.getMaxLifetimeKm().compareTo(BigDecimal.ZERO) > 0
                        && t.getTotalLifetimeKm().divide(t.getMaxLifetimeKm(), 2, java.math.RoundingMode.HALF_UP)
                               .compareTo(new BigDecimal("0.85")) >= 0)
                .count();
        if (retreading == 0 && nearEndOfLife == 0) return;

        StringBuilder body = new StringBuilder();
        if (nearEndOfLife > 0) body.append(nearEndOfLife).append(" tyre(s) at 85%+ life used. ");
        if (retreading > 0) body.append(retreading).append(" tyre(s) currently in retreading. ");
        body.append("Review tyre status and plan replacements.");

        notificationService.sendToRoles(tenant, OFFICE_ROLES,
                NotificationType.DAILY_SUMMARY,
                "Tyre Alert — Replacements Needed",
                body.toString(),
                Map.of("type", "DAILY_SUMMARY"));
    }

    private void sendFleetStatusAlert(Tenant tenant) {
        long available  = vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenant.getId(), VehicleStatusType.AVAILABLE);
        long onTrip     = vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenant.getId(), VehicleStatusType.ON_TRIP);
        long breakdown  = vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenant.getId(), VehicleStatusType.BREAKDOWN);
        long inRepair   = vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenant.getId(), VehicleStatusType.IN_REPAIR);
        notificationService.sendToRoles(tenant, OFFICE_ROLES,
                NotificationType.DAILY_SUMMARY,
                "Fleet Status — 10 AM",
                "Available: " + available + " | In Transit: " + onTrip
                        + " | Breakdown: " + breakdown + " | In Repair: " + inRepair,
                Map.of("type", "DAILY_SUMMARY"));
    }

    private void sendAttendanceSummary(Tenant tenant, LocalDate today) {
        List<Attendance> todayRecords = attendanceRepository
                .findByTenantIdAndAttendanceDateAndIsActiveTrue(tenant.getId(), today);
        if (todayRecords.isEmpty()) return;
        long present  = todayRecords.size();
        long approved = todayRecords.stream()
                .filter(a -> a.getApprovalStatus() == AttendanceApprovalStatus.APPROVED).count();
        long pending  = todayRecords.stream()
                .filter(a -> a.getApprovalStatus() == AttendanceApprovalStatus.PENDING).count();
        notificationService.sendToRoles(tenant, OFFICE_ROLES,
                NotificationType.DAILY_SUMMARY,
                "Attendance Summary — " + today,
                "Present: " + present + " | Approved: " + approved + " | Pending: " + pending,
                Map.of("type", "DAILY_SUMMARY"));
    }

    private void sendDriverVehicleSummary(Tenant tenant, LocalDate today) {
        List<Attendance> todayRecords = attendanceRepository
                .findByTenantIdAndAttendanceDateAndIsActiveTrue(tenant.getId(), today);

        long driversPresent = todayRecords.stream()
                .filter(a -> a.getUser() != null && a.getUser().getRoles() != null
                        && a.getUser().getRoles().stream().anyMatch(r ->
                                r.getName() == RoleName.DRIVER || r.getName() == RoleName.CLEANER))
                .count();
        if (driversPresent == 0) return;

        long onVehicle =
                staffAllocationRepository.countDistinctUsersByTenantIdAndRoleAndStatus(
                        tenant.getId(), RoleName.DRIVER, StaffAllocationStatus.ALLOCATED)
                + staffAllocationRepository.countDistinctUsersByTenantIdAndRoleAndStatus(
                        tenant.getId(), RoleName.DRIVER, StaffAllocationStatus.IN_TRANSIT)
                + staffAllocationRepository.countDistinctUsersByTenantIdAndRoleAndStatus(
                        tenant.getId(), RoleName.CLEANER, StaffAllocationStatus.ALLOCATED)
                + staffAllocationRepository.countDistinctUsersByTenantIdAndRoleAndStatus(
                        tenant.getId(), RoleName.CLEANER, StaffAllocationStatus.IN_TRANSIT);

        long withoutVehicle = Math.max(0, driversPresent - onVehicle);
        notificationService.sendToRoles(tenant, OFFICE_ROLES,
                NotificationType.DAILY_SUMMARY,
                "Driver & Cleaner Status — 6 PM",
                "Present: " + driversPresent + " | On Vehicle: " + onVehicle
                        + " | Without Vehicle: " + withoutVehicle,
                Map.of("type", "DAILY_SUMMARY"));
    }
}
