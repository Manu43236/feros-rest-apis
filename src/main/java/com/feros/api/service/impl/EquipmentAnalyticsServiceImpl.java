package com.feros.api.service.impl;

import com.feros.api.dto.response.EquipmentAnalyticsResponse;
import com.feros.api.dto.response.MachineAnalyticsRow;
import com.feros.api.entity.Equipment;
import com.feros.api.entity.EquipmentDailyLog;
import com.feros.api.entity.EquipmentServiceRecord;
import com.feros.api.entity.MachineAssignment;
import com.feros.api.enums.EquipmentInvoiceStatus;
import com.feros.api.repository.*;
import com.feros.api.service.EquipmentAnalyticsService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentAnalyticsServiceImpl implements EquipmentAnalyticsService {

    private static final double SHIFT_HOURS_PER_DAY = 8.0;
    private static final List<EquipmentInvoiceStatus> BILLED_STATUSES =
        List.of(EquipmentInvoiceStatus.SENT, EquipmentInvoiceStatus.PARTIALLY_PAID, EquipmentInvoiceStatus.PAID);

    private final EquipmentRepository equipmentRepository;
    private final MachineAssignmentRepository assignmentRepository;
    private final EquipmentDailyLogRepository dailyLogRepository;
    private final EquipmentServiceRepository serviceRepository;
    private final EquipmentInvoiceItemRepository invoiceItemRepository;

    @Override
    @Transactional(readOnly = true)
    public EquipmentAnalyticsResponse getAnalytics(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Equipment> machines = equipmentRepository.findByTenantId(tenantId)
                .stream().filter(e -> Boolean.TRUE.equals(e.getIsActive())).toList();

        List<MachineAnalyticsRow> rows = new ArrayList<>();

        for (Equipment eq : machines) {
            List<MachineAssignment> assignments =
                assignmentRepository.findByEquipment_IdAndWorkOrder_Tenant_Id(eq.getId(), tenantId);

            // Deployed days = sum of days each assignment overlaps with [from, to]
            int deployedDays = 0;
            List<Long> assignmentIds = new ArrayList<>();
            for (MachineAssignment a : assignments) {
                LocalDate hireFrom = a.getOnHireDate() != null ? a.getOnHireDate() : a.getStartDate();
                LocalDate hireTo   = a.getOffHireDate() != null ? a.getOffHireDate()
                                   : (a.getEndDate() != null ? a.getEndDate() : to);
                if (hireFrom == null) continue;
                LocalDate overlapStart = hireFrom.isBefore(from) ? from : hireFrom;
                LocalDate overlapEnd   = hireTo.isAfter(to) ? to : hireTo;
                if (!overlapStart.isAfter(overlapEnd)) {
                    deployedDays += (int) ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
                    assignmentIds.add(a.getId());
                }
            }

            if (deployedDays == 0 && assignmentIds.isEmpty()) continue; // machine never deployed in range

            double shiftHours = deployedDays * SHIFT_HOURS_PER_DAY;

            // Daily logs in range
            List<EquipmentDailyLog> logs = assignmentIds.isEmpty()
                ? List.of()
                : dailyLogRepository.findByMachineAssignmentIdInAndLogDateBetween(assignmentIds, from, to);

            double workingHours = logs.stream()
                .mapToDouble(l -> l.getWorkingHours() != null ? l.getWorkingHours().doubleValue() : 0)
                .sum();
            double breakdownHours = logs.stream()
                .mapToDouble(l -> l.getBreakdownHours() != null ? l.getBreakdownHours().doubleValue() : 0)
                .sum();

            double utilizationPct = shiftHours > 0
                ? Math.min(100.0, round1(workingHours / shiftHours * 100)) : 0;
            double availabilityPct = shiftHours > 0
                ? round1(Math.max(0.0, (shiftHours - breakdownHours) / shiftHours * 100)) : 100.0;

            // Revenue from invoices
            BigDecimal revenue = invoiceItemRepository.sumRevenueByEquipment(eq.getId(), tenantId, BILLED_STATUSES, from, to);
            if (revenue == null) revenue = BigDecimal.ZERO;

            // Service costs in range
            List<EquipmentServiceRecord> serviceRecords =
                serviceRepository.findByEquipmentIdAndTenantIdAndIsActiveTrueOrderByCreatedAtDesc(eq.getId(), tenantId);
            BigDecimal serviceCosts = serviceRecords.stream()
                .filter(r -> r.getServiceDate() != null
                    && !r.getServiceDate().isBefore(from)
                    && !r.getServiceDate().isAfter(to)
                    && r.getTotalCost() != null)
                .map(EquipmentServiceRecord::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Depreciation = (deployedDays / 30) × monthlyDepreciation
            BigDecimal monthlyDep = eq.getMonthlyDepreciation() != null ? eq.getMonthlyDepreciation() : BigDecimal.ZERO;
            BigDecimal depreciation = monthlyDep
                .multiply(BigDecimal.valueOf(deployedDays))
                .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

            BigDecimal netProfit = revenue.subtract(serviceCosts).subtract(depreciation);

            rows.add(MachineAnalyticsRow.builder()
                .equipmentId(eq.getId())
                .serialNumber(eq.getSerialNumber())
                .equipmentTypeName(eq.getEquipmentType() != null ? eq.getEquipmentType().getName() : null)
                .makeName(null) // make not directly on Equipment entity
                .deployedDays(deployedDays)
                .shiftHours(shiftHours)
                .workingHours(round1(workingHours))
                .breakdownHours(round1(breakdownHours))
                .utilizationPct(utilizationPct)
                .availabilityPct(availabilityPct)
                .revenue(revenue)
                .serviceCosts(serviceCosts)
                .depreciation(depreciation)
                .netProfit(netProfit)
                .build());
        }

        BigDecimal totalRevenue     = rows.stream().map(MachineAnalyticsRow::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalServiceCosts = rows.stream().map(MachineAnalyticsRow::getServiceCosts).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDepreciation = rows.stream().map(MachineAnalyticsRow::getDepreciation).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNetProfit    = rows.stream().map(MachineAnalyticsRow::getNetProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
        double avgUtil  = rows.isEmpty() ? 0 : round1(rows.stream().mapToDouble(MachineAnalyticsRow::getUtilizationPct).average().orElse(0));
        double avgAvail = rows.isEmpty() ? 0 : round1(rows.stream().mapToDouble(MachineAnalyticsRow::getAvailabilityPct).average().orElse(0));

        return EquipmentAnalyticsResponse.builder()
            .machines(rows)
            .totalRevenue(totalRevenue)
            .totalServiceCosts(totalServiceCosts)
            .totalDepreciation(totalDepreciation)
            .totalNetProfit(totalNetProfit)
            .avgUtilizationPct(avgUtil)
            .avgAvailabilityPct(avgAvail)
            .build();
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
