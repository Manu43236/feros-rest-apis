package com.feros.api.service.impl;

import com.feros.api.dto.response.report.*;
import com.feros.api.entity.*;
import com.feros.api.enums.LrStatus;
import com.feros.api.enums.OrderStatus;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.enums.InvoiceStatus;
import com.feros.api.entity.Client;
import com.feros.api.entity.Vehicle;
import com.feros.api.repository.*;
import com.feros.api.service.ReportService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final VehicleRepository vehicleRepository;
    private final VehicleFuelLogRepository fuelLogRepository;
    private final VehicleMeterReadingRepository meterReadingRepository;
    private final VehicleBreakdownRepository breakdownRepository;
    private final VehicleDocumentRepository documentRepository;
    private final VehicleServiceRepository vehicleServiceRepository;
    private final AttendanceRepository attendanceRepository;
    private final LrRepository lrRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final InvoiceCreditNoteRepository invoiceCreditNoteRepository;
    private final LrTripExpenseRepository tripExpenseRepository;
    private final LrTripExpenseItemRepository tripExpenseItemRepository;
    private final InvoiceLrRepository invoiceLrRepository;

    // ── 1. Fleet Status ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FleetStatusRow> getFleetStatus(LocalDate date) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .map(v -> FleetStatusRow.builder()
                        .vehicleId(v.getId())
                        .registrationNumber(v.getRegistrationNumber())
                        .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : "—")
                        .ownershipType(v.getOwnershipType() != null ? v.getOwnershipType().getName() : "—")
                        .currentStatus(v.getCurrentStatus() != null ? v.getCurrentStatus().getStatusType().name() : "UNKNOWN")
                        .currentDriverName(v.getCurrentDriver() != null ? v.getCurrentDriver().getName() : "—")
                        .currentCleanerName(v.getCurrentCleaner() != null ? v.getCurrentCleaner().getName() : "—")
                        .tripScope(v.getTripScope() != null ? v.getTripScope().name() : "—")
                        .build())
                .toList();
    }

    // ── 2. Fuel & Mileage ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FuelMileageRow> getFuelMileage(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Vehicle> vehicles = vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId);
        List<VehicleFuelLog> logs = fuelLogRepository.findByTenantIdAndDateRange(tenantId, start, end);
        List<VehicleMeterReading> readings = meterReadingRepository.findByTenantIdAndDateRange(tenantId, start, end);

        Map<Long, List<VehicleFuelLog>> logsByVehicle = logs.stream()
                .collect(Collectors.groupingBy(f -> f.getVehicle().getId()));
        Map<Long, List<VehicleMeterReading>> readingsByVehicle = readings.stream()
                .collect(Collectors.groupingBy(m -> m.getVehicle().getId()));

        return vehicles.stream().map(v -> {
            List<VehicleFuelLog> vLogs = logsByVehicle.getOrDefault(v.getId(), List.of());
            List<VehicleMeterReading> vReadings = readingsByVehicle.getOrDefault(v.getId(), List.of());

            BigDecimal totalLitres = vLogs.stream()
                    .map(VehicleFuelLog::getLitresFilled)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCost = vLogs.stream()
                    .map(VehicleFuelLog::getTotalCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal openOdo = vReadings.stream()
                    .map(VehicleMeterReading::getReadingKm)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            BigDecimal closeOdo = vReadings.stream()
                    .map(VehicleMeterReading::getReadingKm)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            BigDecimal totalKm = (openOdo != null && closeOdo != null)
                    ? closeOdo.subtract(openOdo).max(BigDecimal.ZERO)
                    : null;

            BigDecimal mileage = null;
            if (totalKm != null && totalLitres.compareTo(BigDecimal.ZERO) > 0) {
                mileage = totalKm.divide(totalLitres, 2, RoundingMode.HALF_UP);
            }

            return FuelMileageRow.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : "—")
                    .fillCount(vLogs.size())
                    .totalLitresFilled(totalLitres)
                    .totalFuelCost(totalCost)
                    .openingOdometer(openOdo)
                    .closingOdometer(closeOdo)
                    .totalKm(totalKm)
                    .mileageKmPerLitre(mileage)
                    .build();
        }).toList();
    }

    // ── 4. Breakdown Report ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BreakdownReportRow> getBreakdowns(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return breakdownRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .map(b -> {
                    Long daysLost = null;
                    if (b.getResolvedAt() != null) {
                        daysLost = Math.max(0L, ChronoUnit.DAYS.between(
                                b.getBreakdownDate().toLocalDate(),
                                b.getResolvedAt().toLocalDate()));
                    }
                    return BreakdownReportRow.builder()
                            .vehicleId(b.getVehicle().getId())
                            .registrationNumber(b.getVehicle().getRegistrationNumber())
                            .vehicleType(b.getVehicle().getVehicleType() != null
                                    ? b.getVehicle().getVehicleType().getName() : "—")
                            .breakdownDate(b.getBreakdownDate())
                            .location(b.getLocation())
                            .breakdownType(b.getBreakdownType().name())
                            .reason(b.getReason())
                            .status(b.getStatus().name())
                            .daysLost(daysLost)
                            .reportedBy(b.getReportedBy() != null ? b.getReportedBy().getName() : "—")
                            .build();
                })
                .toList();
    }

    // ── 5. Document Expiry ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DocumentExpiryRow> getDocumentExpiry(int days) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        return documentRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(d -> d.getExpiryDate() != null)
                .map(d -> {
                    long daysLeft = ChronoUnit.DAYS.between(today, d.getExpiryDate());
                    String status = daysLeft < 0 ? "RED" : daysLeft <= days ? "AMBER" : "GREEN";
                    return DocumentExpiryRow.builder()
                            .vehicleId(d.getVehicle().getId())
                            .registrationNumber(d.getVehicle().getRegistrationNumber())
                            .vehicleType(d.getVehicle().getVehicleType() != null
                                    ? d.getVehicle().getVehicleType().getName() : "—")
                            .documentType(d.getDocumentType().getName())
                            .documentNumber(d.getDocumentNumber())
                            .expiryDate(d.getExpiryDate())
                            .daysLeft(daysLeft)
                            .expiryStatus(status)
                            .build();
                })
                .sorted(Comparator.comparingLong(DocumentExpiryRow::getDaysLeft))
                .toList();
    }

    // ── 6. Maintenance & Service ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceServiceRow> getMaintenanceService(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return vehicleServiceRepository.findByTenantIdAndServiceDateBetweenAndIsActiveTrue(tenantId, startDate, endDate)
                .stream()
                .map(s -> MaintenanceServiceRow.builder()
                        .vehicleId(s.getVehicle().getId())
                        .registrationNumber(s.getVehicle().getRegistrationNumber())
                        .vehicleType(s.getVehicle().getVehicleType() != null
                                ? s.getVehicle().getVehicleType().getName() : "—")
                        .serviceNumber(s.getServiceNumber())
                        .serviceDate(s.getServiceDate())
                        .completedDate(s.getCompletedDate())
                        .serviceType(s.getServiceType().name())
                        .triggeredBy(s.getTriggeredBy().name())
                        .taskCount(s.getTasks() != null ? s.getTasks().size() : 0)
                        .totalCost(s.getTotalCost())
                        .status(s.getStatus().name())
                        .vendorName(s.getVendorName())
                        .nextServiceDueOdometer(s.getDueAtOdometer())
                        .build())
                .toList();
    }

    // ── 7. Attendance Daily Register ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDailyRow> getAttendanceDaily(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Attendance> records = attendanceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        Map<Long, String> userVehicleMap = buildUserVehicleMap(tenantId);

        return records.stream().map(a -> {
            Double hoursWorked = null;
            if (a.getMarkedAt() != null && a.getMarkedOutAt() != null) {
                long minutes = Duration.between(a.getMarkedAt(), a.getMarkedOutAt()).toMinutes();
                hoursWorked = Math.round(minutes / 60.0 * 100.0) / 100.0;
            }
            return AttendanceDailyRow.builder()
                    .employeeId(a.getUser().getId())
                    .employeeName(a.getUser().getName())
                    .role(primaryRole(a.getUser()))
                    .vehicleRegistrationNumber(userVehicleMap.getOrDefault(a.getUser().getId(), "—"))
                    .attendanceDate(a.getAttendanceDate())
                    .attendanceType(a.getAttendanceType().getName())
                    .markedAt(a.getMarkedAt())
                    .markedOutAt(a.getMarkedOutAt())
                    .hoursWorked(hoursWorked)
                    .approvalStatus(a.getApprovalStatus().name())
                    .leaveType(a.getLeaveType() != null ? a.getLeaveType().getName() : null)
                    .remarks(a.getRemarks())
                    .build();
        }).toList();
    }

    // ── 8. Attendance Monthly Summary ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummaryRow> getAttendanceSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Attendance> records = attendanceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        Map<Long, String> userVehicleMap = buildUserVehicleMap(tenantId);

        Map<Long, List<Attendance>> byUser = records.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        return byUser.entrySet().stream().map(entry -> {
            List<Attendance> userRecords = entry.getValue();
            Attendance first = userRecords.get(0);

            int present = 0, absent = 0, leave = 0, half = 0, other = 0;
            for (Attendance a : userRecords) {
                String t = a.getAttendanceType().getName().toUpperCase();
                if      (t.contains("PRESENT")) present++;
                else if (t.contains("ABSENT"))  absent++;
                else if (t.contains("LEAVE"))   leave++;
                else if (t.contains("HALF"))    half++;
                else                            other++;
            }

            int total = userRecords.size();
            BigDecimal pct = total > 0
                    ? BigDecimal.valueOf(present * 100.0 / total).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return AttendanceSummaryRow.builder()
                    .employeeId(entry.getKey())
                    .employeeName(first.getUser().getName())
                    .role(primaryRole(first.getUser()))
                    .vehicleRegistrationNumber(userVehicleMap.getOrDefault(entry.getKey(), "—"))
                    .presentDays(present)
                    .absentDays(absent)
                    .leaveDays(leave)
                    .halfDays(half)
                    .otherDays(other)
                    .totalRecords(total)
                    .presentPercent(pct)
                    .build();
        }).sorted(Comparator.comparing(AttendanceSummaryRow::getEmployeeName)).toList();
    }

    // ── 9. LR Register ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LrRegisterRow> getLrRegister(LocalDate startDate, LocalDate endDate, Long clientId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = clientId != null
                ? lrRepository.findByTenantIdAndDateRangeAndClient(tenantId, startDate, endDate, clientId)
                : lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);

        return lrs.stream().map(l -> LrRegisterRow.builder()
                .lrId(l.getId())
                .lrNumber(l.getLrNumber())
                .lrDate(l.getLrDate())
                .orderNumber(l.getOrder().getOrderNumber())
                .clientName(l.getOrder().getClient().getClientName())
                .vehicleRegistrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                .driverName(l.getDriver() != null ? l.getDriver().getName() : "—")
                .cleanerName(l.getCleaner() != null ? l.getCleaner().getName() : "—")
                .fromCity(l.getOrder().getSourceCity() != null ? l.getOrder().getSourceCity().getName() : "—")
                .fromState(l.getOrder().getSourceState() != null ? l.getOrder().getSourceState().getName() : "—")
                .toCity(l.getOrder().getDestinationCity() != null ? l.getOrder().getDestinationCity().getName() : "—")
                .toState(l.getOrder().getDestinationState() != null ? l.getOrder().getDestinationState().getName() : "—")
                .materialType(l.getOrder().getMaterialType() != null ? l.getOrder().getMaterialType().getName() : "—")
                .allocatedWeight(l.getAllocatedWeight())
                .loadedWeight(l.getLoadedWeight())
                .deliveredWeight(l.getDeliveredWeight())
                .weightVariance(l.getWeightVariance())
                .isOverloaded(l.getIsOverloaded())
                .loadedAt(l.getLoadedAt())
                .deliveredAt(l.getDeliveredAt())
                .ewayBillNumber(l.getEwayBillNumber())
                .ewayBillDate(l.getEwayBillDate())
                .ewayBillValidUpto(l.getEwayBillValidUpto())
                .lrStatus(l.getLrStatus() != null ? l.getLrStatus().name() : "—")
                .remarks(l.getRemarks())
                .build()).toList();
    }

    // ── 10. Weight Discrepancy ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<WeightDiscrepancyRow> getWeightDiscrepancy(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .filter(l -> (l.getWeightVariance() != null && l.getWeightVariance().compareTo(BigDecimal.ZERO) != 0)
                        || Boolean.TRUE.equals(l.getIsOverloaded()))
                .map(l -> WeightDiscrepancyRow.builder()
                        .lrId(l.getId())
                        .lrNumber(l.getLrNumber())
                        .lrDate(l.getLrDate())
                        .clientName(l.getOrder().getClient().getClientName())
                        .vehicleRegistrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                        .fromCity(l.getOrder().getSourceCity() != null ? l.getOrder().getSourceCity().getName() : "—")
                        .toCity(l.getOrder().getDestinationCity() != null ? l.getOrder().getDestinationCity().getName() : "—")
                        .materialType(l.getOrder().getMaterialType() != null ? l.getOrder().getMaterialType().getName() : "—")
                        .allocatedWeight(l.getAllocatedWeight())
                        .loadedWeight(l.getLoadedWeight())
                        .deliveredWeight(l.getDeliveredWeight())
                        .weightVariance(l.getWeightVariance())
                        .isOverloaded(l.getIsOverloaded())
                        .lrStatus(l.getLrStatus() != null ? l.getLrStatus().name() : "—")
                        .build())
                .toList();
    }

    // ── 11. Delayed Deliveries ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DelayedDeliveryRow> getDelayedDeliveries(LocalDate startDate, LocalDate endDate, int thresholdDays) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDateTime now = LocalDateTime.now();

        return lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .filter(l -> l.getLrStatus() == LrStatus.IN_TRANSIT || l.getLrStatus() == LrStatus.DELIVERED)
                .filter(l -> l.getLoadedAt() != null)
                .filter(l -> {
                    LocalDateTime endTime = l.getDeliveredAt() != null ? l.getDeliveredAt() : now;
                    return java.time.temporal.ChronoUnit.DAYS.between(l.getLoadedAt(), endTime) >= thresholdDays;
                })
                .map(l -> {
                    LocalDateTime endTime = l.getDeliveredAt() != null ? l.getDeliveredAt() : now;
                    long days = java.time.temporal.ChronoUnit.DAYS.between(l.getLoadedAt(), endTime);
                    return buildDelayedRow(l, days);
                })
                .sorted(Comparator.comparingLong(DelayedDeliveryRow::getDaysInTransit).reversed())
                .toList();
    }

    // ── 14. Order Register ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderRegisterRow> getOrderRegister(LocalDate startDate, LocalDate endDate, String status) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<com.feros.api.entity.Order> orders = status != null
                ? orderRepository.findByTenantIdAndDateRangeAndStatus(tenantId, startDate, endDate, OrderStatus.valueOf(status))
                : orderRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);

        return orders.stream().map(o -> OrderRegisterRow.builder()
                .orderId(o.getId())
                .orderNumber(o.getOrderNumber())
                .orderDate(o.getOrderDate())
                .expectedDeliveryDate(o.getExpectedDeliveryDate())
                .clientName(o.getClient().getClientName())
                .materialType(o.getMaterialType() != null ? o.getMaterialType().getName() : "—")
                .fromCity(o.getSourceCity() != null ? o.getSourceCity().getName() : "—")
                .fromState(o.getSourceState() != null ? o.getSourceState().getName() : "—")
                .toCity(o.getDestinationCity() != null ? o.getDestinationCity().getName() : "—")
                .toState(o.getDestinationState() != null ? o.getDestinationState().getName() : "—")
                .totalWeight(o.getTotalWeight())
                .totalWeightFulfilled(o.getTotalWeightFulfilled())
                .freightRateType(o.getFreightRateType() != null ? o.getFreightRateType().name() : "—")
                .freightRate(o.getFreightRate())
                .totalFreightAmount(o.getTotalFreightAmount())
                .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : "—")
                .orderPaymentStatus(o.getOrderPaymentStatus() != null ? o.getOrderPaymentStatus().name() : "—")
                .build()).toList();
    }

    // ── 15. Open Orders ───────────────────────────────────────────────────────────

    private static final List<OrderStatus> OPEN_STATUSES = List.of(
            OrderStatus.PENDING, OrderStatus.PARTIALLY_ASSIGNED, OrderStatus.FULLY_ASSIGNED,
            OrderStatus.IN_TRANSIT, OrderStatus.PARTIALLY_DELIVERED);

    @Override
    @Transactional(readOnly = true)
    public List<OpenOrderRow> getOpenOrders() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return orderRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(o -> OPEN_STATUSES.contains(o.getOrderStatus()))
                .map(o -> {
                    BigDecimal pending = o.getTotalWeight().subtract(
                            o.getTotalWeightFulfilled() != null ? o.getTotalWeightFulfilled() : BigDecimal.ZERO).max(BigDecimal.ZERO);
                    return OpenOrderRow.builder()
                            .orderId(o.getId())
                            .orderNumber(o.getOrderNumber())
                            .orderDate(o.getOrderDate())
                            .expectedDeliveryDate(o.getExpectedDeliveryDate())
                            .clientName(o.getClient().getClientName())
                            .materialType(o.getMaterialType() != null ? o.getMaterialType().getName() : "—")
                            .fromCity(o.getSourceCity() != null ? o.getSourceCity().getName() : "—")
                            .toCity(o.getDestinationCity() != null ? o.getDestinationCity().getName() : "—")
                            .totalWeight(o.getTotalWeight())
                            .totalWeightFulfilled(o.getTotalWeightFulfilled())
                            .pendingWeight(pending)
                            .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : "—")
                            .build();
                })
                .sorted(Comparator.comparing(r -> r.getOrderDate() != null ? r.getOrderDate() : java.time.LocalDate.MIN))
                .toList();
    }

    // ── 16. Order Client Summary ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderClientSummaryRow> getOrderClientSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Map<Long, List<com.feros.api.entity.Order>> byClient = orderRepository
                .findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .collect(Collectors.groupingBy(o -> o.getClient().getId()));

        return byClient.entrySet().stream().map(entry -> {
            List<com.feros.api.entity.Order> orders = entry.getValue();
            com.feros.api.entity.Order first = orders.get(0);
            return OrderClientSummaryRow.builder()
                    .clientId(entry.getKey())
                    .clientName(first.getClient().getClientName())
                    .totalOrders(orders.size())
                    .completedOrders((int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED || o.getOrderStatus() == OrderStatus.DELIVERED).count())
                    .inProgressOrders((int) orders.stream().filter(o -> OPEN_STATUSES.contains(o.getOrderStatus())).count())
                    .cancelledOrders((int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED).count())
                    .totalWeight(orders.stream().map(com.feros.api.entity.Order::getTotalWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalWeightFulfilled(orders.stream().map(com.feros.api.entity.Order::getTotalWeightFulfilled).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalFreightAmount(orders.stream().map(com.feros.api.entity.Order::getTotalFreightAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .build();
        }).sorted(Comparator.comparing(OrderClientSummaryRow::getClientName)).toList();
    }

    // ── 17. Overdue Orders ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OverdueOrderRow> getOverdueOrders(int thresholdDays) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        return orderRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(o -> OPEN_STATUSES.contains(o.getOrderStatus()))
                .filter(o -> o.getExpectedDeliveryDate() != null)
                .filter(o -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(o.getExpectedDeliveryDate(), today);
                    return days >= thresholdDays;
                })
                .map(o -> {
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(o.getExpectedDeliveryDate(), today);
                    return OverdueOrderRow.builder()
                            .orderId(o.getId())
                            .orderNumber(o.getOrderNumber())
                            .orderDate(o.getOrderDate())
                            .expectedDeliveryDate(o.getExpectedDeliveryDate())
                            .daysOverdue(daysOverdue)
                            .clientName(o.getClient().getClientName())
                            .materialType(o.getMaterialType() != null ? o.getMaterialType().getName() : "—")
                            .fromCity(o.getSourceCity() != null ? o.getSourceCity().getName() : "—")
                            .toCity(o.getDestinationCity() != null ? o.getDestinationCity().getName() : "—")
                            .totalWeight(o.getTotalWeight())
                            .totalWeightFulfilled(o.getTotalWeightFulfilled())
                            .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : "—")
                            .build();
                })
                .sorted(Comparator.comparingLong(OverdueOrderRow::getDaysOverdue).reversed())
                .toList();
    }

    // ── 18. Weight Fulfillment ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<WeightFulfillmentRow> getWeightFulfillment(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return orderRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .map(o -> {
                    BigDecimal fulfilled = o.getTotalWeightFulfilled() != null ? o.getTotalWeightFulfilled() : BigDecimal.ZERO;
                    BigDecimal pending = o.getTotalWeight().subtract(fulfilled).max(BigDecimal.ZERO);
                    BigDecimal pct = o.getTotalWeight().compareTo(BigDecimal.ZERO) > 0
                            ? fulfilled.multiply(BigDecimal.valueOf(100)).divide(o.getTotalWeight(), 1, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return WeightFulfillmentRow.builder()
                            .orderId(o.getId())
                            .orderNumber(o.getOrderNumber())
                            .orderDate(o.getOrderDate())
                            .clientName(o.getClient().getClientName())
                            .materialType(o.getMaterialType() != null ? o.getMaterialType().getName() : "—")
                            .fromCity(o.getSourceCity() != null ? o.getSourceCity().getName() : "—")
                            .toCity(o.getDestinationCity() != null ? o.getDestinationCity().getName() : "—")
                            .totalWeight(o.getTotalWeight())
                            .totalWeightFulfilled(fulfilled)
                            .pendingWeight(pending)
                            .fulfillmentPercent(pct)
                            .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : "—")
                            .build();
                })
                .sorted(Comparator.comparing(WeightFulfillmentRow::getFulfillmentPercent))
                .toList();
    }

    // ── 19. Route-wise Order Summary ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderRouteSummaryRow> getOrderRouteSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<com.feros.api.entity.Order> orders = orderRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        Map<String, List<com.feros.api.entity.Order>> byRoute = orders.stream()
                .collect(Collectors.groupingBy(o ->
                        (o.getSourceCity() != null ? o.getSourceCity().getName() : "—") + "|" +
                        (o.getDestinationCity() != null ? o.getDestinationCity().getName() : "—")));

        return byRoute.entrySet().stream().map(entry -> {
            List<com.feros.api.entity.Order> routeOrders = entry.getValue();
            com.feros.api.entity.Order first = routeOrders.get(0);
            return OrderRouteSummaryRow.builder()
                    .fromCity(first.getSourceCity() != null ? first.getSourceCity().getName() : "—")
                    .fromState(first.getSourceState() != null ? first.getSourceState().getName() : "—")
                    .toCity(first.getDestinationCity() != null ? first.getDestinationCity().getName() : "—")
                    .toState(first.getDestinationState() != null ? first.getDestinationState().getName() : "—")
                    .totalOrders(routeOrders.size())
                    .completedOrders((int) routeOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED || o.getOrderStatus() == OrderStatus.DELIVERED).count())
                    .totalWeight(routeOrders.stream().map(com.feros.api.entity.Order::getTotalWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalWeightFulfilled(routeOrders.stream().map(com.feros.api.entity.Order::getTotalWeightFulfilled).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalFreightAmount(routeOrders.stream().map(com.feros.api.entity.Order::getTotalFreightAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .build();
        }).sorted(Comparator.comparingInt(OrderRouteSummaryRow::getTotalOrders).reversed()).toList();
    }

    // ── 20. Order Payment Status ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderPaymentStatusRow> getOrderPaymentStatus(LocalDate startDate, LocalDate endDate, String paymentStatus) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return orderRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .filter(o -> paymentStatus == null || o.getOrderPaymentStatus() == OrderPaymentStatus.valueOf(paymentStatus))
                .map(o -> OrderPaymentStatusRow.builder()
                        .orderId(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .orderDate(o.getOrderDate())
                        .clientName(o.getClient().getClientName())
                        .totalFreightAmount(o.getTotalFreightAmount())
                        .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : "—")
                        .orderPaymentStatus(o.getOrderPaymentStatus() != null ? o.getOrderPaymentStatus().name() : "—")
                        .build())
                .sorted(Comparator.comparing(OrderPaymentStatusRow::getClientName))
                .toList();
    }

    // ── 12. Vehicle Trip Summary ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<VehicleTripSummaryRow> getVehicleTripSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);

        Map<Long, List<Lr>> byVehicle = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getVehicleAllocation().getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<Lr> vLrs = entry.getValue();
            Lr first = vLrs.get(0);
            Vehicle vehicle = first.getVehicleAllocation().getVehicle();

            return VehicleTripSummaryRow.builder()
                    .vehicleId(vehicle.getId())
                    .registrationNumber(vehicle.getRegistrationNumber())
                    .vehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().getName() : "—")
                    .totalTrips(vLrs.size())
                    .completedTrips((int) vLrs.stream().filter(l -> l.getLrStatus() == LrStatus.DELIVERED).count())
                    .inTransitTrips((int) vLrs.stream().filter(l -> l.getLrStatus() == LrStatus.IN_TRANSIT).count())
                    .cancelledTrips((int) vLrs.stream().filter(l -> l.getLrStatus() == LrStatus.CANCELLED).count())
                    .totalAllocatedWeight(vLrs.stream().map(Lr::getAllocatedWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalLoadedWeight(vLrs.stream().map(Lr::getLoadedWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalDeliveredWeight(vLrs.stream().map(Lr::getDeliveredWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .build();
        }).sorted(Comparator.comparing(VehicleTripSummaryRow::getRegistrationNumber)).toList();
    }

    // ── 13. Client Trip Summary ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ClientTripSummaryRow> getClientTripSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);

        Map<Long, List<Lr>> byClient = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getOrder().getClient().getId()));

        return byClient.entrySet().stream().map(entry -> {
            List<Lr> cLrs = entry.getValue();
            Client client = cLrs.get(0).getOrder().getClient();

            return ClientTripSummaryRow.builder()
                    .clientId(client.getId())
                    .clientName(client.getClientName())
                    .totalTrips(cLrs.size())
                    .completedTrips((int) cLrs.stream().filter(l -> l.getLrStatus() == LrStatus.DELIVERED).count())
                    .inTransitTrips((int) cLrs.stream().filter(l -> l.getLrStatus() == LrStatus.IN_TRANSIT).count())
                    .cancelledTrips((int) cLrs.stream().filter(l -> l.getLrStatus() == LrStatus.CANCELLED).count())
                    .totalAllocatedWeight(cLrs.stream().map(Lr::getAllocatedWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalLoadedWeight(cLrs.stream().map(Lr::getLoadedWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .totalDeliveredWeight(cLrs.stream().map(Lr::getDeliveredWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .build();
        }).sorted(Comparator.comparing(ClientTripSummaryRow::getClientName)).toList();
    }

    private DelayedDeliveryRow buildDelayedRow(Lr l, long days) {
        return DelayedDeliveryRow.builder()
                .lrId(l.getId())
                .lrNumber(l.getLrNumber())
                .lrDate(l.getLrDate())
                .clientName(l.getOrder().getClient().getClientName())
                .vehicleRegistrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                .driverName(l.getDriver() != null ? l.getDriver().getName() : "—")
                .fromCity(l.getOrder().getSourceCity() != null ? l.getOrder().getSourceCity().getName() : "—")
                .toCity(l.getOrder().getDestinationCity() != null ? l.getOrder().getDestinationCity().getName() : "—")
                .materialType(l.getOrder().getMaterialType() != null ? l.getOrder().getMaterialType().getName() : "—")
                .loadedAt(l.getLoadedAt())
                .daysInTransit(days)
                .lrStatus(l.getLrStatus() != null ? l.getLrStatus().name() : "—")
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private Map<Long, String> buildUserVehicleMap(Long tenantId) {
        return vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .flatMap(v -> {
                    List<Map.Entry<Long, String>> entries = new ArrayList<>();
                    if (v.getCurrentDriver() != null)
                        entries.add(Map.entry(v.getCurrentDriver().getId(), v.getRegistrationNumber()));
                    if (v.getCurrentCleaner() != null)
                        entries.add(Map.entry(v.getCurrentCleaner().getId(), v.getRegistrationNumber()));
                    return entries.stream();
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
    }

    // ── Invoice Register ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceRegisterRow> getInvoiceRegister(LocalDate startDate, LocalDate endDate, String status) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        InvoiceStatus statusEnum = status != null ? InvoiceStatus.valueOf(status) : null;
        return invoiceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .filter(i -> statusEnum == null || i.getInvoiceStatus() == statusEnum)
                .map(i -> InvoiceRegisterRow.builder()
                        .invoiceId(i.getId())
                        .invoiceNumber(i.getInvoiceNumber())
                        .invoiceDate(i.getInvoiceDate())
                        .dueDate(i.getDueDate())
                        .clientName(i.getClient().getClientName())
                        .subtotal(i.getSubtotal())
                        .taxAmount(i.getTaxAmount())
                        .totalAmount(i.getTotalAmount())
                        .amountPaid(i.getAmountPaid())
                        .balanceDue(i.getBalanceDue())
                        .invoiceStatus(i.getInvoiceStatus() != null ? i.getInvoiceStatus().name() : "—")
                        .build())
                .toList();
    }

    // ── Outstanding Invoices ──────────────────────────────────────────────────────

    private static final List<InvoiceStatus> OUTSTANDING_STATUSES = List.of(
            InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);

    @Override
    @Transactional(readOnly = true)
    public List<OutstandingInvoiceRow> getOutstandingInvoices() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = LocalDate.now();
        return invoiceRepository.findOutstandingInvoices(tenantId).stream()
                .filter(i -> i.getInvoiceStatus() != null && OUTSTANDING_STATUSES.contains(i.getInvoiceStatus()))
                .map(i -> {
                    Long daysOverdue = (i.getDueDate() != null && today.isAfter(i.getDueDate()))
                            ? ChronoUnit.DAYS.between(i.getDueDate(), today) : null;
                    return OutstandingInvoiceRow.builder()
                            .invoiceId(i.getId())
                            .invoiceNumber(i.getInvoiceNumber())
                            .invoiceDate(i.getInvoiceDate())
                            .dueDate(i.getDueDate())
                            .clientName(i.getClient().getClientName())
                            .totalAmount(i.getTotalAmount())
                            .amountPaid(i.getAmountPaid())
                            .balanceDue(i.getBalanceDue())
                            .invoiceStatus(i.getInvoiceStatus().name())
                            .daysOverdue(daysOverdue)
                            .build();
                })
                .sorted(Comparator.comparing(r -> r.getDueDate() != null ? r.getDueDate() : LocalDate.MAX))
                .toList();
    }

    // ── Invoice Aging ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceAgingRow> getInvoiceAging() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = LocalDate.now();
        return invoiceRepository.findOutstandingInvoices(tenantId).stream()
                .filter(i -> i.getInvoiceStatus() != null && OUTSTANDING_STATUSES.contains(i.getInvoiceStatus()))
                .filter(i -> i.getDueDate() != null && today.isAfter(i.getDueDate()))
                .map(i -> {
                    long days = ChronoUnit.DAYS.between(i.getDueDate(), today);
                    String bucket = days <= 30 ? "1-30 days" : days <= 60 ? "31-60 days" : days <= 90 ? "61-90 days" : "90+ days";
                    return InvoiceAgingRow.builder()
                            .invoiceId(i.getId())
                            .invoiceNumber(i.getInvoiceNumber())
                            .invoiceDate(i.getInvoiceDate())
                            .dueDate(i.getDueDate())
                            .clientName(i.getClient().getClientName())
                            .balanceDue(i.getBalanceDue())
                            .daysOverdue(days)
                            .agingBucket(bucket)
                            .build();
                })
                .sorted(Comparator.comparingLong(InvoiceAgingRow::getDaysOverdue).reversed())
                .toList();
    }

    // ── Collections ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRow> getCollections(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return invoicePaymentRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .map(p -> CollectionRow.builder()
                        .paymentId(p.getId())
                        .paymentDate(p.getPaymentDate())
                        .invoiceNumber(p.getInvoice().getInvoiceNumber())
                        .clientName(p.getInvoice().getClient().getClientName())
                        .amount(p.getAmount())
                        .paymentMode(p.getPaymentMode() != null ? p.getPaymentMode().name() : "—")
                        .referenceNumber(p.getReferenceNumber())
                        .remarks(p.getRemarks())
                        .build())
                .toList();
    }

    // ── Credit Note Register ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CreditNoteRegisterRow> getCreditNoteRegister(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return invoiceCreditNoteRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .map(cn -> CreditNoteRegisterRow.builder()
                        .creditNoteId(cn.getId())
                        .creditNoteNumber(cn.getCreditNoteNumber())
                        .creditNoteDate(cn.getCreditNoteDate())
                        .clientName(cn.getClient().getClientName())
                        .linkedInvoiceNumber(cn.getInvoice() != null ? cn.getInvoice().getInvoiceNumber() : null)
                        .amount(cn.getAmount())
                        .reason(cn.getReason())
                        .creditNoteStatus(cn.getCreditNoteStatus() != null ? cn.getCreditNoteStatus().name() : "—")
                        .build())
                .toList();
    }

    // ── Trip Expenses ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TripExpenseReportRow> getTripExpenses(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tripExpenseRepository.findByTenantIdAndLrDateRange(tenantId, startDate, endDate).stream()
                .map(e -> {
                    Lr lr = e.getLr();
                    BigDecimal itemsTotal = tripExpenseItemRepository.findByTripExpenseIdAndIsActiveTrue(e.getId())
                            .stream().map(i -> i.getAmount() != null ? i.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal batta = e.getDriverBatta().add(e.getCleanerBatta()).add(e.getTripMamulu());
                    BigDecimal total = batta.add(itemsTotal);
                    return TripExpenseReportRow.builder()
                            .lrId(lr.getId())
                            .lrNumber(lr.getLrNumber())
                            .lrDate(lr.getLrDate())
                            .vehicleNumber(lr.getVehicleAllocation().getVehicle().getRegistrationNumber())
                            .driverName(lr.getDriver() != null ? lr.getDriver().getName() : "—")
                            .cleanerName(lr.getCleaner() != null ? lr.getCleaner().getName() : "—")
                            .fromCity(lr.getOrder().getSourceCity() != null ? lr.getOrder().getSourceCity().getName() : "—")
                            .toCity(lr.getOrder().getDestinationCity() != null ? lr.getOrder().getDestinationCity().getName() : "—")
                            .advanceAmount(e.getAdvanceAmount())
                            .driverBatta(e.getDriverBatta())
                            .cleanerBatta(e.getCleanerBatta())
                            .tripMamulu(e.getTripMamulu())
                            .itemsTotal(itemsTotal)
                            .totalExpense(total)
                            .settlementAmount(e.getSettlementAmount())
                            .status(e.getStatus() != null ? e.getStatus().name() : "—")
                            .build();
                })
                .toList();
    }

    // ── Fuel Cost Summary ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FuelCostRow> getFuelCostSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<VehicleFuelLog> logs = fuelLogRepository.findByTenantIdAndDateRange(
                tenantId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        Map<Long, List<VehicleFuelLog>> byVehicle = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<VehicleFuelLog> vLogs = entry.getValue();
            Vehicle vehicle = vLogs.get(0).getVehicle();
            BigDecimal totalLitres = vLogs.stream().map(VehicleFuelLog::getLitresFilled).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCost = vLogs.stream().map(VehicleFuelLog::getTotalCost).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return FuelCostRow.builder()
                    .vehicleId(vehicle.getId())
                    .registrationNumber(vehicle.getRegistrationNumber())
                    .vehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().getName() : "—")
                    .totalFills(vLogs.size())
                    .totalLitres(totalLitres)
                    .totalCost(totalCost)
                    .build();
        }).sorted(Comparator.comparing(FuelCostRow::getRegistrationNumber)).toList();
    }

    // ── Maintenance Cost Summary ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceCostRow> getMaintenanceCostSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<VehicleService> services = vehicleServiceRepository
                .findByTenantIdAndServiceDateBetweenAndIsActiveTrue(tenantId, startDate, endDate);

        Map<Long, List<VehicleService>> byVehicle = services.stream()
                .collect(Collectors.groupingBy(s -> s.getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<VehicleService> vServices = entry.getValue();
            Vehicle vehicle = vServices.get(0).getVehicle();
            BigDecimal totalCost = vServices.stream().map(VehicleService::getTotalCost).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return MaintenanceCostRow.builder()
                    .vehicleId(vehicle.getId())
                    .registrationNumber(vehicle.getRegistrationNumber())
                    .vehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().getName() : "—")
                    .totalServices(vServices.size())
                    .totalCost(totalCost)
                    .build();
        }).sorted(Comparator.comparing(MaintenanceCostRow::getRegistrationNumber)).toList();
    }

    // ── Document Cost Summary ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DocumentCostRow> getDocumentCostSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<VehicleDocument> docs = documentRepository.findByTenantIdAndPaidOnBetween(tenantId, startDate, endDate);
        return docs.stream().map(d -> DocumentCostRow.builder()
                .vehicleId(d.getVehicle().getId())
                .registrationNumber(d.getVehicle().getRegistrationNumber())
                .vehicleType(d.getVehicle().getVehicleType() != null ? d.getVehicle().getVehicleType().getName() : "—")
                .documentTypeName(d.getDocumentType().getName())
                .documentNumber(d.getDocumentNumber())
                .issuerName(d.getIssuerName())
                .paidOn(d.getPaidOn())
                .cost(d.getCost())
                .build()
        ).toList();
    }

    // ── Driver Performance ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DriverPerformanceRow> getDriverPerformance(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate)
                .stream().filter(l -> l.getDriver() != null).toList();

        List<Attendance> attendance = attendanceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        Map<Long, List<Attendance>> attendanceByUser = attendance.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        Map<Long, List<Lr>> byDriver = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getDriver().getId()));

        return byDriver.entrySet().stream().map(entry -> {
            List<Lr> driverLrs = entry.getValue();
            User driver = driverLrs.get(0).getDriver();

            BigDecimal totalWeight = driverLrs.stream()
                    .map(Lr::getLoadedWeight).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Lr> delivered = driverLrs.stream()
                    .filter(l -> l.getDeliveredAt() != null).toList();
            long onTime = delivered.stream().filter(l -> {
                LocalDate expected = l.getOrder().getExpectedDeliveryDate();
                return expected != null && !l.getDeliveredAt().toLocalDate().isAfter(expected);
            }).count();

            List<Attendance> driverAtt = attendanceByUser.getOrDefault(driver.getId(), List.of());
            int present = (int) driverAtt.stream()
                    .filter(a -> a.getAttendanceType().getName().toUpperCase().contains("PRESENT")).count();
            int total = driverAtt.size();
            BigDecimal attPct = total > 0
                    ? BigDecimal.valueOf(present * 100.0 / total).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal onTimePct = !delivered.isEmpty()
                    ? BigDecimal.valueOf(onTime * 100.0 / delivered.size()).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return DriverPerformanceRow.builder()
                    .driverId(driver.getId())
                    .driverName(driver.getName())
                    .totalTrips(driverLrs.size())
                    .totalWeight(totalWeight)
                    .deliveredTrips(delivered.size())
                    .onTimeDeliveries((int) onTime)
                    .onTimePct(onTimePct)
                    .presentDays(present)
                    .totalAttendanceDays(total)
                    .attendancePct(attPct)
                    .build();
        }).sorted(Comparator.comparing(DriverPerformanceRow::getDriverName)).toList();
    }

    // ── Cleaner Performance ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CleanerPerformanceRow> getCleanerPerformance(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate)
                .stream().filter(l -> l.getCleaner() != null).toList();

        List<Attendance> attendance = attendanceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        Map<Long, List<Attendance>> attendanceByUser = attendance.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        Map<Long, List<Lr>> byCleaner = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getCleaner().getId()));

        return byCleaner.entrySet().stream().map(entry -> {
            List<Lr> cleanerLrs = entry.getValue();
            User cleaner = cleanerLrs.get(0).getCleaner();

            BigDecimal totalWeight = cleanerLrs.stream()
                    .map(Lr::getLoadedWeight).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Attendance> cleanerAtt = attendanceByUser.getOrDefault(cleaner.getId(), List.of());
            int present = (int) cleanerAtt.stream()
                    .filter(a -> a.getAttendanceType().getName().toUpperCase().contains("PRESENT")).count();
            int total = cleanerAtt.size();
            BigDecimal attPct = total > 0
                    ? BigDecimal.valueOf(present * 100.0 / total).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return CleanerPerformanceRow.builder()
                    .cleanerId(cleaner.getId())
                    .cleanerName(cleaner.getName())
                    .totalTrips(cleanerLrs.size())
                    .totalWeight(totalWeight)
                    .presentDays(present)
                    .totalAttendanceDays(total)
                    .attendancePct(attPct)
                    .build();
        }).sorted(Comparator.comparing(CleanerPerformanceRow::getCleanerName)).toList();
    }

    // ── P&L Summary ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PnlSummaryRow getPnlSummary(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        List<InvoiceLr> invoiceLrs = invoiceLrRepository.findByTenantIdAndInvoiceDateRange(tenantId, startDate, endDate);
        BigDecimal totalInvoiced = invoiceLrs.stream()
                .map(il -> il.getTotalAmount() != null ? il.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = invoicePaymentRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate)
                .stream().map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balanceDue = totalInvoiced.subtract(totalCollected);

        List<LrTripExpense> expenses = tripExpenseRepository.findByTenantIdAndLrDateRange(tenantId, startDate, endDate);
        Map<Long, BigDecimal> itemTotalById = buildItemTotalMap(expenses);
        BigDecimal tripExpenses = expenses.stream()
                .map(e -> tripTotal(e, itemTotalById))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        BigDecimal fuelExpenses = fuelLogRepository.findByTenantIdAndDateRange(tenantId, start, end)
                .stream().map(l -> l.getTotalCost() != null ? l.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal maintenanceExpenses = vehicleServiceRepository
                .findByTenantIdAndServiceDateBetweenAndIsActiveTrue(tenantId, startDate, endDate)
                .stream().map(s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal documentExpenses = documentRepository.findByTenantIdAndPaidOnBetween(tenantId, startDate, endDate)
                .stream().map(d -> d.getCost() != null ? d.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = tripExpenses.add(fuelExpenses).add(maintenanceExpenses).add(documentExpenses);

        return PnlSummaryRow.builder()
                .totalInvoiced(totalInvoiced)
                .totalCollected(totalCollected)
                .balanceDue(balanceDue)
                .tripExpenses(tripExpenses)
                .fuelExpenses(fuelExpenses)
                .maintenanceExpenses(maintenanceExpenses)
                .documentExpenses(documentExpenses)
                .totalExpenses(totalExpenses)
                .grossPnl(totalInvoiced.subtract(tripExpenses))
                .netPnl(totalInvoiced.subtract(totalExpenses))
                .build();
    }

    // ── P&L Per Client ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ClientPnlRow> getClientPnl(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        List<InvoiceLr> invoiceLrs = invoiceLrRepository.findByTenantIdAndInvoiceDateRange(tenantId, startDate, endDate);
        Map<Long, List<InvoiceLr>> byClient = invoiceLrs.stream()
                .collect(Collectors.groupingBy(il -> il.getOrder().getClient().getId()));

        List<LrTripExpense> expenses = tripExpenseRepository.findByTenantIdAndLrDateRange(tenantId, startDate, endDate);
        Map<Long, BigDecimal> itemTotalById = buildItemTotalMap(expenses);
        Map<Long, BigDecimal> expByClient = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getLr().getOrder().getClient().getId(),
                        Collectors.reducing(BigDecimal.ZERO, e -> tripTotal(e, itemTotalById), BigDecimal::add)
                ));

        Map<Long, BigDecimal> collectedByClient = invoicePaymentRepository
                .findByTenantIdAndDateRange(tenantId, startDate, endDate).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getInvoice().getClient().getId(),
                        Collectors.reducing(BigDecimal.ZERO, p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO, BigDecimal::add)
                ));

        return byClient.entrySet().stream().map(entry -> {
            Long clientId = entry.getKey();
            List<InvoiceLr> cLrs = entry.getValue();
            Client client = cLrs.get(0).getOrder().getClient();
            BigDecimal revenue = cLrs.stream()
                    .map(il -> il.getTotalAmount() != null ? il.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal collected = collectedByClient.getOrDefault(clientId, BigDecimal.ZERO);
            BigDecimal tripExp = expByClient.getOrDefault(clientId, BigDecimal.ZERO);
            return ClientPnlRow.builder()
                    .clientId(clientId)
                    .clientName(client.getClientName())
                    .totalInvoiced(revenue)
                    .totalCollected(collected)
                    .balanceDue(revenue.subtract(collected))
                    .tripExpenses(tripExp)
                    .netPnl(revenue.subtract(tripExp))
                    .build();
        }).sorted(Comparator.comparing(ClientPnlRow::getClientName)).toList();
    }

    // ── P&L Per Vehicle ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<VehiclePnlRow> getVehiclePnl(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        List<InvoiceLr> invoiceLrs = invoiceLrRepository.findByTenantIdAndInvoiceDateRange(tenantId, startDate, endDate);
        Map<Long, List<InvoiceLr>> byVehicle = invoiceLrs.stream()
                .filter(il -> il.getLr().getVehicleAllocation() != null && il.getLr().getVehicleAllocation().getVehicle() != null)
                .collect(Collectors.groupingBy(il -> il.getLr().getVehicleAllocation().getVehicle().getId()));

        List<LrTripExpense> expenses = tripExpenseRepository.findByTenantIdAndLrDateRange(tenantId, startDate, endDate);
        Map<Long, BigDecimal> itemTotalById = buildItemTotalMap(expenses);
        Map<Long, BigDecimal> tripExpByVehicle = expenses.stream()
                .filter(e -> e.getLr().getVehicleAllocation() != null && e.getLr().getVehicleAllocation().getVehicle() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getLr().getVehicleAllocation().getVehicle().getId(),
                        Collectors.reducing(BigDecimal.ZERO, e -> tripTotal(e, itemTotalById), BigDecimal::add)
                ));

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        Map<Long, BigDecimal> fuelByVehicle = fuelLogRepository.findByTenantIdAndDateRange(tenantId, start, end).stream()
                .collect(Collectors.groupingBy(l -> l.getVehicle().getId(),
                        Collectors.reducing(BigDecimal.ZERO, l -> l.getTotalCost() != null ? l.getTotalCost() : BigDecimal.ZERO, BigDecimal::add)));

        Map<Long, BigDecimal> maintByVehicle = vehicleServiceRepository
                .findByTenantIdAndServiceDateBetweenAndIsActiveTrue(tenantId, startDate, endDate).stream()
                .collect(Collectors.groupingBy(s -> s.getVehicle().getId(),
                        Collectors.reducing(BigDecimal.ZERO, s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO, BigDecimal::add)));

        Map<Long, BigDecimal> docByVehicle = documentRepository.findByTenantIdAndPaidOnBetween(tenantId, startDate, endDate).stream()
                .collect(Collectors.groupingBy(d -> d.getVehicle().getId(),
                        Collectors.reducing(BigDecimal.ZERO, d -> d.getCost() != null ? d.getCost() : BigDecimal.ZERO, BigDecimal::add)));

        return byVehicle.entrySet().stream().map(entry -> {
            Long vehicleId = entry.getKey();
            List<InvoiceLr> vLrs = entry.getValue();
            Vehicle vehicle = vLrs.get(0).getLr().getVehicleAllocation().getVehicle();
            BigDecimal revenue = vLrs.stream()
                    .map(il -> il.getTotalAmount() != null ? il.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tripExp = tripExpByVehicle.getOrDefault(vehicleId, BigDecimal.ZERO);
            BigDecimal fuel = fuelByVehicle.getOrDefault(vehicleId, BigDecimal.ZERO);
            BigDecimal maint = maintByVehicle.getOrDefault(vehicleId, BigDecimal.ZERO);
            BigDecimal doc = docByVehicle.getOrDefault(vehicleId, BigDecimal.ZERO);
            BigDecimal totalExp = tripExp.add(fuel).add(maint).add(doc);
            return VehiclePnlRow.builder()
                    .vehicleId(vehicleId)
                    .registrationNumber(vehicle.getRegistrationNumber())
                    .vehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().getName() : "—")
                    .revenue(revenue)
                    .tripExpenses(tripExp)
                    .fuelCost(fuel)
                    .maintenanceCost(maint)
                    .documentCost(doc)
                    .totalExpenses(totalExp)
                    .netPnl(revenue.subtract(totalExp))
                    .build();
        }).sorted(Comparator.comparing(VehiclePnlRow::getRegistrationNumber)).toList();
    }

    // ── P&L Per Route ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoutePnlRow> getRoutePnl(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        List<InvoiceLr> invoiceLrs = invoiceLrRepository.findByTenantIdAndInvoiceDateRange(tenantId, startDate, endDate);
        Map<String, List<InvoiceLr>> byRoute = invoiceLrs.stream()
                .filter(il -> il.getOrder().getSourceCity() != null && il.getOrder().getDestinationCity() != null)
                .collect(Collectors.groupingBy(il ->
                        il.getOrder().getSourceCity().getName() + "||" + il.getOrder().getDestinationCity().getName()));

        List<LrTripExpense> expenses = tripExpenseRepository.findByTenantIdAndLrDateRange(tenantId, startDate, endDate);
        Map<Long, BigDecimal> itemTotalById = buildItemTotalMap(expenses);
        Map<String, BigDecimal> expByRoute = expenses.stream()
                .filter(e -> e.getLr().getOrder().getSourceCity() != null && e.getLr().getOrder().getDestinationCity() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getLr().getOrder().getSourceCity().getName() + "||" + e.getLr().getOrder().getDestinationCity().getName(),
                        Collectors.reducing(BigDecimal.ZERO, e -> tripTotal(e, itemTotalById), BigDecimal::add)
                ));

        return byRoute.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("\\|\\|", 2);
            String fromCity = parts[0];
            String toCity = parts.length > 1 ? parts[1] : "—";
            List<InvoiceLr> routeLrs = entry.getValue();
            BigDecimal revenue = routeLrs.stream()
                    .map(il -> il.getTotalAmount() != null ? il.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tripExp = expByRoute.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            return RoutePnlRow.builder()
                    .fromCity(fromCity)
                    .toCity(toCity)
                    .totalTrips(routeLrs.size())
                    .revenue(revenue)
                    .tripExpenses(tripExp)
                    .netPnl(revenue.subtract(tripExp))
                    .build();
        }).sorted(Comparator.comparing(RoutePnlRow::getFromCity).thenComparing(RoutePnlRow::getToCity)).toList();
    }

    // ── P&L Helpers ───────────────────────────────────────────────────────────────

    private Map<Long, BigDecimal> buildItemTotalMap(List<LrTripExpense> expenses) {
        if (expenses.isEmpty()) return Map.of();
        List<Long> ids = expenses.stream().map(LrTripExpense::getId).toList();
        return tripExpenseItemRepository.findByTripExpenseIdsAndIsActiveTrue(ids).stream()
                .collect(Collectors.groupingBy(
                        i -> i.getTripExpense().getId(),
                        Collectors.reducing(BigDecimal.ZERO, i -> i.getAmount() != null ? i.getAmount() : BigDecimal.ZERO, BigDecimal::add)
                ));
    }

    private BigDecimal tripTotal(LrTripExpense e, Map<Long, BigDecimal> itemTotalById) {
        BigDecimal batta = e.getDriverBatta().add(e.getCleanerBatta()).add(e.getTripMamulu());
        return batta.add(itemTotalById.getOrDefault(e.getId(), BigDecimal.ZERO));
    }

    private String primaryRole(User user) {
        Set<String> names = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        for (String r : List.of("DRIVER", "CLEANER", "SUPERVISOR", "OFFICE_STAFF", "SERVICE_MEN", "STORE_KEEPER", "ADMIN")) {
            if (names.contains(r)) return r;
        }
        return names.isEmpty() ? "—" : names.iterator().next();
    }
}
