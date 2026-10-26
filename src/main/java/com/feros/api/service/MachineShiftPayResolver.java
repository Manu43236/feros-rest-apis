package com.feros.api.service;

import com.feros.api.entity.Equipment;
import com.feros.api.entity.MachineShiftAssignment;
import com.feros.api.repository.MachineShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-(user, date) machine-shift facts for a pay cycle: the snapshotted shift pay and the machine the
 * person was on. Built once per run from {@link MachineShiftAssignmentRepository}. A user is credited
 * the operator pay on shifts where they are the operator and the cleaner pay where they are the cleaner;
 * two shifts on the same day sum. Payroll uses this to replace the daily rate on shift days, exactly as
 * {@link LeaseShiftPayResolver} does for lease crew. Equipment mirror of that resolver.
 */
@Component
@RequiredArgsConstructor
public class MachineShiftPayResolver {

    private final MachineShiftAssignmentRepository machineShiftAssignmentRepository;

    /** pay = summed snapshot pay for the day; machine = the (any) shift machine that day. */
    public record Day(BigDecimal pay, Equipment machine) {}

    /** userId → (date → Day). */
    public record Context(Map<Long, Map<LocalDate, Day>> byUser) {}

    public Context buildContext(Long tenantId, LocalDate startDate, LocalDate endDate) {
        Map<Long, Map<LocalDate, Day>> byUser = new HashMap<>();
        for (MachineShiftAssignment a : machineShiftAssignmentRepository
                .findOverlappingByTenantId(tenantId, startDate, endDate)) {
            Equipment machine = a.getMachineAssignment().getEquipment();
            if (a.getOperatorStaff() != null && a.getOperatorStaff().getUser() != null) {
                add(byUser, a.getOperatorStaff().getUser().getId(), a.getWorkDate(),
                        a.getOperatorPaySnapshot(), machine);
            }
            if (a.getCleanerStaff() != null && a.getCleanerStaff().getUser() != null) {
                add(byUser, a.getCleanerStaff().getUser().getId(), a.getWorkDate(),
                        a.getCleanerPaySnapshot(), machine);
            }
        }
        return new Context(byUser);
    }

    private static void add(Map<Long, Map<LocalDate, Day>> byUser, Long userId, LocalDate date,
                            BigDecimal pay, Equipment machine) {
        BigDecimal p = pay != null ? pay : BigDecimal.ZERO;
        byUser.computeIfAbsent(userId, k -> new HashMap<>())
                .merge(date, new Day(p, machine),
                        (oldV, newV) -> new Day(oldV.pay().add(newV.pay()), oldV.machine()));
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

    public Equipment machine(Context ctx, Long userId, LocalDate date) {
        Map<LocalDate, Day> m = ctx.byUser().get(userId);
        if (m == null) return null;
        Day d = m.get(date);
        return d != null ? d.machine() : null;
    }
}
