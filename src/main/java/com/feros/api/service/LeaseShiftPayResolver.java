package com.feros.api.service;

import com.feros.api.entity.LeaseShiftAssignment;
import com.feros.api.entity.Vehicle;
import com.feros.api.repository.LeaseShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-(user, date) lease-shift facts for a pay cycle: the snapshotted shift pay and the vehicle the
 * person was on. Built once per run from {@link LeaseShiftAssignmentRepository}. A user is credited
 * the driver pay on shifts where they are the driver and the cleaner pay where they are the cleaner;
 * two shifts on the same day sum. Payroll uses this to replace the daily rate on shift days, and the
 * three vehicle-day resolvers use {@link #vehicle} to place a shift driver/cleaner on their vehicle.
 */
@Component
@RequiredArgsConstructor
public class LeaseShiftPayResolver {

    private final LeaseShiftAssignmentRepository leaseShiftAssignmentRepository;

    /** pay = summed snapshot pay for the day; vehicle = the (any) shift vehicle that day. */
    public record Day(BigDecimal pay, Vehicle vehicle) {}

    /** userId → (date → Day). */
    public record Context(Map<Long, Map<LocalDate, Day>> byUser) {}

    public Context buildContext(Long tenantId, LocalDate startDate, LocalDate endDate) {
        Map<Long, Map<LocalDate, Day>> byUser = new HashMap<>();
        for (LeaseShiftAssignment a : leaseShiftAssignmentRepository
                .findOverlappingByTenantId(tenantId, startDate, endDate)) {
            Vehicle vehicle = a.getLeaseVehicleAssignment().getVehicle();
            if (a.getDriverStaff() != null && a.getDriverStaff().getUser() != null) {
                add(byUser, a.getDriverStaff().getUser().getId(), a.getWorkDate(),
                        a.getDriverPaySnapshot(), vehicle);
            }
            if (a.getCleanerStaff() != null && a.getCleanerStaff().getUser() != null) {
                add(byUser, a.getCleanerStaff().getUser().getId(), a.getWorkDate(),
                        a.getCleanerPaySnapshot(), vehicle);
            }
        }
        return new Context(byUser);
    }

    private static void add(Map<Long, Map<LocalDate, Day>> byUser, Long userId, LocalDate date,
                            BigDecimal pay, Vehicle vehicle) {
        BigDecimal p = pay != null ? pay : BigDecimal.ZERO;
        byUser.computeIfAbsent(userId, k -> new HashMap<>())
                .merge(date, new Day(p, vehicle),
                        (oldV, newV) -> new Day(oldV.pay().add(newV.pay()), oldV.vehicle()));
    }

    public boolean isShiftDay(Context ctx, Long userId, LocalDate date) {
        Map<LocalDate, Day> m = ctx.byUser().get(userId);
        return m != null && m.containsKey(date);
    }

    public BigDecimal shiftPay(Context ctx, Long userId, LocalDate date) {
        Map<LocalDate, Day> m = ctx.byUser().get(userId);
        if (m == null) return BigDecimal.ZERO;
        Day d = m.get(date);
        return d != null ? d.pay() : BigDecimal.ZERO;
    }

    public Vehicle vehicle(Context ctx, Long userId, LocalDate date) {
        Map<LocalDate, Day> m = ctx.byUser().get(userId);
        if (m == null) return null;
        Day d = m.get(date);
        return d != null ? d.vehicle() : null;
    }
}
