package com.feros.api.service;

import com.feros.api.entity.LeaseDriverAssignmentLog;
import com.feros.api.entity.User;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleStaffAssignment;
import com.feros.api.repository.LeaseDriverAssignmentLogRepository;
import com.feros.api.repository.OrderStaffAllocationRepository;
import com.feros.api.repository.VehicleStaffAssignmentRepository;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private final OrderStaffAllocationRepository orderStaffAllocationRepository;

    /** Tenant-wide maps, built once per run and reused for every (user, date) resolution. */
    public record Context(Map<Long, List<VehicleStaffAssignment>> userAssignments,
                          Map<Long, Vehicle> leaseVehicleByUser,
                          Map<Long, Long> leaseHolderByVehicle) {}

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

        // userId → their latest lease vehicle (mirrors buildLeaseDriverMap: latest driver per vehicle,
        // then latest vehicle per driver), but keeps the Vehicle instead of the reg string.
        Map<Long, Vehicle> leaseVehicleByUser = logs.stream()
                .collect(Collectors.toMap(
                        l -> l.getLeaseVehicleAssignment().getVehicle().getId(),
                        l -> l,
                        (a, b) -> a.getAssignedAt().isAfter(b.getAssignedAt()) ? a : b))
                .values().stream()
                .collect(Collectors.toMap(
                        l -> l.getDriverStaff().getUser().getId(),
                        l -> l,
                        (a, b) -> a.getAssignedAt().isAfter(b.getAssignedAt()) ? a : b))
                .values().stream()
                .collect(Collectors.toMap(
                        l -> l.getDriverStaff().getUser().getId(),
                        l -> l.getLeaseVehicleAssignment().getVehicle()));

        return new Context(userAssignments, leaseVehicleByUser, leaseHolderByVehicle);
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
            Long leaseHolder = ctx.leaseHolderByVehicle().get(vehicleId);
            boolean leaseDisplaced = leaseHolder != null && !leaseHolder.equals(userId)
                    && "DRIVER".equals(myRole);
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
        // fallback 2 — assigned via lease, not VSA
        return ctx.leaseVehicleByUser().get(userId);
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
