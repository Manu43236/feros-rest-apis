package com.feros.api.service;

import com.feros.api.entity.LeaseCleanerAssignmentLog;
import com.feros.api.entity.LeaseDriverAssignmentLog;
import com.feros.api.entity.User;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleStaffAssignment;
import com.feros.api.repository.LeaseCleanerAssignmentLogRepository;
import com.feros.api.repository.LeaseDriverAssignmentLogRepository;
import com.feros.api.repository.OrderStaffAllocationRepository;
import com.feros.api.repository.VehicleStaffAssignmentRepository;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * THE single source of truth for "which vehicle was this staff member on, on this day."
 *
 * <p>This is a faithful extraction of {@code ReportServiceImpl.resolveVehicleForDate} (the
 * admin-trusted attendance-report logic), returning the {@link Vehicle} instead of a reg string.
 * The attendance report, payroll, and the payslip annexure all resolve through this one method so a
 * payslip's vehicle (and therefore its vehicle allowance) can never disagree with the attendance
 * report. Resolution order: standing VSA (with swap-suppression + lease-displacement) → active order
 * trip → lease. Lease handling is collapsed-latest, identical to the report — NOT changed here.
 */
@Component
@RequiredArgsConstructor
public class StaffVehicleDayResolver {

    private final VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;
    private final LeaseDriverAssignmentLogRepository leaseDriverAssignmentLogRepository;
    private final LeaseCleanerAssignmentLogRepository leaseCleanerAssignmentLogRepository;
    private final OrderStaffAllocationRepository orderStaffAllocationRepository;
    private final LeaseShiftPayResolver leaseShiftPayResolver;

    /** One lease log, flattened to the date window it covers. {@code to} is null while still
     *  assigned. {@code assignedAt} breaks the handover-day tie (latest assignment wins the day). */
    public record LeaseSpan(Long userId, Long vehicleId, Vehicle vehicle,
                            LocalDate from, LocalDate to, LocalDateTime assignedAt) {}

    /** Tenant-wide data, built once per run and reused for every (user, date) resolution.
     *  {@code driverSpans}/{@code cleanerSpans} keep every lease log (NOT collapsed) so a vehicle
     *  shared by several drivers in the period resolves per day instead of only to its last holder;
     *  holder maps are kept per-role so a cleaner's VSA is only displaced by the lease <em>cleaner</em>. */
    public record Context(Map<Long, List<VehicleStaffAssignment>> userAssignments,
                          List<LeaseSpan> driverSpans,
                          List<LeaseSpan> cleanerSpans,
                          Map<Long, Long> leaseHolderByVehicle,
                          Map<Long, Long> leaseCleanerHolderByVehicle,
                          LeaseShiftPayResolver.Context shiftCtx) {}

    public Context buildContext(Long tenantId, LocalDate startDate, LocalDate endDate) {
        Map<Long, List<VehicleStaffAssignment>> userAssignments = vehicleStaffAssignmentRepository
                .findOverlappingForTenant(tenantId, startDate, endDate)
                .stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        List<LeaseDriverAssignmentLog> logs = leaseDriverAssignmentLogRepository
                .findOverlappingByTenantId(tenantId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
                .stream()
                .filter(l -> l.getDriverStaff() != null && l.getDriverStaff().getUser() != null)
                .toList();

        // vehicleId → userId of the latest lease driver holding that vehicle (mirrors buildLeaseHolderByVehicle)
        Map<Long, Long> leaseHolderByVehicle = logs.stream()
                .collect(Collectors.toMap(
                        l -> l.getLeaseVehicleAssignment().getVehicle().getId(),
                        l -> l,
                        (a, b) -> a.getAssignedAt().isAfter(b.getAssignedAt()) ? a : b))
                .values().stream()
                .collect(Collectors.toMap(
                        l -> l.getLeaseVehicleAssignment().getVehicle().getId(),
                        l -> l.getDriverStaff().getUser().getId()));

        // Every driver lease log, flattened to its date window — NOT collapsed. resolve() picks the
        // span covering the resolution date, so a vehicle shared by several drivers in the period is
        // resolved per day instead of only to its last holder (the old collapse dropped the rest).
        List<LeaseSpan> driverSpans = logs.stream()
                .map(l -> new LeaseSpan(
                        l.getDriverStaff().getUser().getId(),
                        l.getLeaseVehicleAssignment().getVehicle().getId(),
                        l.getLeaseVehicleAssignment().getVehicle(),
                        l.getAssignedAt().toLocalDate(),
                        l.getUnassignedAt() != null ? l.getUnassignedAt().toLocalDate() : null,
                        l.getAssignedAt()))
                .toList();

        // Same for lease cleaners — a lease vehicle never gets a VSA row, so the cleaner's
        // attendance/payroll vehicle must resolve from here exactly like the driver's.
        List<LeaseCleanerAssignmentLog> cleanerLogs = leaseCleanerAssignmentLogRepository
                .findOverlappingByTenantId(tenantId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
                .stream()
                .filter(l -> l.getCleanerStaff() != null && l.getCleanerStaff().getUser() != null)
                .toList();

        Map<Long, Long> leaseCleanerHolderByVehicle = cleanerLogs.stream()
                .collect(Collectors.toMap(
                        l -> l.getLeaseVehicleAssignment().getVehicle().getId(),
                        l -> l,
                        (a, b) -> a.getAssignedAt().isAfter(b.getAssignedAt()) ? a : b))
                .values().stream()
                .collect(Collectors.toMap(
                        l -> l.getLeaseVehicleAssignment().getVehicle().getId(),
                        l -> l.getCleanerStaff().getUser().getId()));

        List<LeaseSpan> cleanerSpans = cleanerLogs.stream()
                .map(l -> new LeaseSpan(
                        l.getCleanerStaff().getUser().getId(),
                        l.getLeaseVehicleAssignment().getVehicle().getId(),
                        l.getLeaseVehicleAssignment().getVehicle(),
                        l.getAssignedAt().toLocalDate(),
                        l.getUnassignedAt() != null ? l.getUnassignedAt().toLocalDate() : null,
                        l.getAssignedAt()))
                .toList();

        // Shift-duty leases have no VSA and no standing lease log — the shift assignment is the only
        // record placing the person on a vehicle for the day (date-exact, night shift on start date).
        LeaseShiftPayResolver.Context shiftCtx = leaseShiftPayResolver.buildContext(tenantId, startDate, endDate);

        return new Context(userAssignments, driverSpans, cleanerSpans,
                leaseHolderByVehicle, leaseCleanerHolderByVehicle, shiftCtx);
    }

    /** The vehicle this user was on for {@code date}, or null (== "—"). Verbatim port of
     *  {@code ReportServiceImpl.resolveVehicleForDate}. */
    public Vehicle resolve(Context ctx, Long userId, Long tenantId, LocalDate date) {
        Optional<VehicleStaffAssignment> myVsa = ctx.userAssignments().getOrDefault(userId, List.of()).stream()
                .filter(a -> !a.getAssignedFrom().isAfter(date)
                        && (a.getAssignedTo() == null || !a.getAssignedTo().isBefore(date)))
                .max(Comparator.comparing(VehicleStaffAssignment::getAssignedFrom)
                        .thenComparing(VehicleStaffAssignment::getCreatedAt));
        if (myVsa.isPresent()) {
            Long vehicleId = myVsa.get().getVehicle().getId();
            LocalDate myAssignedFrom = myVsa.get().getAssignedFrom();
            String myRole = primaryRole(myVsa.get().getUser());

            boolean swappedOut = ctx.userAssignments().values().stream()
                    .flatMap(List::stream)
                    .anyMatch(a -> !a.getUser().getId().equals(userId)
                            && a.getVehicle().getId().equals(vehicleId)
                            && primaryRole(a.getUser()).equals(myRole)
                            && !a.getAssignedFrom().isAfter(date)
                            && (a.getAssignedTo() == null || !a.getAssignedTo().isBefore(date))
                            && (a.getAssignedFrom().isAfter(myAssignedFrom)
                                || (a.getAssignedFrom().isEqual(myAssignedFrom)
                                    && a.getCreatedAt() != null && myVsa.get().getCreatedAt() != null
                                    && a.getCreatedAt().isAfter(myVsa.get().getCreatedAt()))));
            Long leaseHolder = "CLEANER".equals(myRole)
                    ? ctx.leaseCleanerHolderByVehicle().get(vehicleId)
                    : ctx.leaseHolderByVehicle().get(vehicleId);
            boolean leaseDisplaced = leaseHolder != null && !leaseHolder.equals(userId)
                    && ("DRIVER".equals(myRole) || "CLEANER".equals(myRole));
            boolean unassignedToday = myVsa.get().getAssignedTo() != null
                    && myVsa.get().getAssignedTo().equals(date)
                    && date.equals(TimeUtil.today());
            if (!swappedOut && !leaseDisplaced && !unassignedToday) {
                return myVsa.get().getVehicle();
            }
        }

        // fallback 1 — no standing assignment, but on an active order trip
        Vehicle fromOrder = orderStaffAllocationRepository
                .findActiveOnDateForUser(userId, tenantId, date)
                .stream().findFirst()
                .map(sa -> sa.getVehicleAllocation().getVehicle())
                .orElse(null);
        if (fromOrder != null) return fromOrder;
        // fallback 2 — assigned via standing lease log, not VSA. Date-aware: the vehicle this user
        // actually holds on `date` (shared/rotated lease vehicles resolve per day, never collapsed).
        Vehicle fromLease = leaseHeldVehicle(ctx.driverSpans(), userId, date);
        if (fromLease == null) fromLease = leaseHeldVehicle(ctx.cleanerSpans(), userId, date);
        if (fromLease != null) return fromLease;
        // fallback 3 — shift-duty lease: date-exact shift assignment
        return leaseShiftPayResolver.vehicle(ctx.shiftCtx(), userId, date);
    }

    /** The lease vehicle this user actually holds on {@code date}. Among all spans covering the day,
     *  the latest-assigned one per vehicle decides that vehicle's holder (handover day → the incoming
     *  driver wins); the user gets their latest-assigned vehicle among those they hold. This is the
     *  per-day equivalent of the single-day attendance screen, and replaces the period-wide collapse
     *  that kept only each vehicle's last holder and dropped every other driver to "—". */
    private Vehicle leaseHeldVehicle(List<LeaseSpan> spans, Long userId, LocalDate date) {
        Map<Long, LeaseSpan> holderByVehicle = spans.stream()
                .filter(s -> !s.from().isAfter(date) && (s.to() == null || !s.to().isBefore(date)))
                .collect(Collectors.toMap(
                        LeaseSpan::vehicleId, s -> s,
                        (a, b) -> a.assignedAt().isAfter(b.assignedAt()) ? a : b));
        return holderByVehicle.values().stream()
                .filter(s -> s.userId().equals(userId))
                .max(Comparator.comparing(LeaseSpan::assignedAt))
                .map(LeaseSpan::vehicle)
                .orElse(null);
    }

    /** Verbatim copy of ReportServiceImpl.primaryRole so resolution logic is byte-for-byte identical. */
    private String primaryRole(User user) {
        java.util.Set<String> names = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        for (String r : List.of("DRIVER", "CLEANER", "SUPERVISOR", "OFFICE_STAFF", "SERVICE_MANAGER", "TECHNICIAN", "STORE_KEEPER", "ADMIN")) {
            if (names.contains(r)) return r;
        }
        return names.isEmpty() ? "—" : names.iterator().next();
    }
}
