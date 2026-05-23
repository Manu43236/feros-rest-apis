package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.response.*;
import com.feros.api.entity.*;
import com.feros.api.enums.*;
import com.feros.api.repository.*;
import com.feros.api.service.ReportsService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsServiceImpl implements ReportsService {

    private final LrRepository lrRepository;
    private final InvoiceRepository invoiceRepository;
    private final PayrollRepository payrollRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final OrderRepository orderRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final UserRepository userRepository;
    private final OrderStaffAllocationRepository staffAllocationRepository;
    private final OrderVehicleAllocationRepository vehicleAllocationRepository;
    private final InvoiceLrRepository invoiceLrRepository;
    private final VehicleBreakdownRepository vehicleBreakdownRepository;
    private final VehicleServiceRepository vehicleServiceRepository;
    private final InvoiceCreditNoteRepository invoiceCreditNoteRepository;
    private final LrChargeRepository lrChargeRepository;
    private final SparePartsInventoryRepository sparePartsInventoryRepository;
    private final SparePartsTransactionRepository sparePartsTransactionRepository;
    private final ServicePartRepository servicePartRepository;
    private final VehicleTyreFittingRepository vehicleTyreFittingRepository;
    private final TyreRepository tyreRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    @Transactional(readOnly = true)
    public List<LrRegisterResponse> getLrRegister(LocalDate from, LocalDate to, Long clientId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = (clientId != null)
                ? lrRepository.findByTenantIdAndDateRangeAndClient(tenantId, from, to, clientId)
                : lrRepository.findByTenantIdAndDateRange(tenantId, from, to);

        return lrs.stream().map(l -> {
            var order = l.getOrder();
            var allocation = l.getVehicleAllocation();
            return LrRegisterResponse.builder()
                    .lrId(l.getId())
                    .lrNumber(l.getLrNumber())
                    .lrDate(l.getLrDate())
                    .orderNumber(order.getOrderNumber())
                    .clientId(order.getClient().getId())
                    .clientName(order.getClient().getClientName())
                    .vehicleRegistrationNumber(allocation.getVehicle().getRegistrationNumber())
                    .fromCity(order.getSourceCity().getName())
                    .fromState(order.getSourceState().getName())
                    .toCity(order.getDestinationCity().getName())
                    .toState(order.getDestinationState().getName())
                    .materialType(order.getMaterialType().getName())
                    .allocatedWeight(l.getAllocatedWeight())
                    .loadedWeight(l.getLoadedWeight())
                    .deliveredWeight(l.getDeliveredWeight())
                    .weightVariance(l.getWeightVariance())
                    .isOverloaded(l.getIsOverloaded())
                    .loadedAt(l.getLoadedAt())
                    .deliveredAt(l.getDeliveredAt())
                    .freightRateType(order.getFreightRateType().name())
                    .freightRate(order.getFreightRate())
                    .lrStatus(l.getLrStatus().name())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutstandingResponse> getInvoiceOutstanding(Long clientId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Invoice> invoices = (clientId != null)
                ? invoiceRepository.findOutstandingInvoicesByClient(tenantId, clientId)
                : invoiceRepository.findOutstandingInvoices(tenantId);

        LocalDate today = TimeUtil.today();
        return invoices.stream().map(i -> {
            long daysOverdue = 0;
            if (i.getDueDate() != null && i.getDueDate().isBefore(today)) {
                daysOverdue = ChronoUnit.DAYS.between(i.getDueDate(), today);
            }
            return InvoiceOutstandingResponse.builder()
                    .invoiceId(i.getId())
                    .invoiceNumber(i.getInvoiceNumber())
                    .invoiceDate(i.getInvoiceDate())
                    .dueDate(i.getDueDate())
                    .clientId(i.getClient().getId())
                    .clientName(i.getClient().getClientName())
                    .totalAmount(i.getTotalAmount())
                    .amountPaid(i.getAmountPaid())
                    .balanceDue(i.getBalanceDue())
                    .invoiceStatus(i.getInvoiceStatus().name())
                    .daysOverdue(daysOverdue)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollSummaryResponse> getPayrollSummary(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Payroll> payrolls = payrollRepository.findByTenantIdAndDateRange(tenantId, from, to);

        return payrolls.stream().map(p -> {
            String roleName = p.getUser().getRoles().stream()
                    .findFirst().map(r -> r.getName().name()).orElse(null);
            return PayrollSummaryResponse.builder()
                    .payrollId(p.getId())
                    .userId(p.getUser().getId())
                    .userName(p.getUser().getName())
                    .userPhone(p.getUser().getPhone())
                    .roleName(roleName)
                    .payCycleStartDate(p.getPayCycleStartDate())
                    .payCycleEndDate(p.getPayCycleEndDate())
                    .totalDays(p.getTotalDays())
                    .presentDays(p.getPresentDays())
                    .absentDays(p.getAbsentDays())
                    .halfDays(p.getHalfDays())
                    .leaveDays(p.getLeaveDays())
                    .dailyRate(p.getDailyRate())
                    .basicPay(p.getBasicPay())
                    .overtimePay(p.getOvertimePay())
                    .tripBonus(p.getTripBonus())
                    .grossPay(p.getGrossPay())
                    .totalDeductions(p.getTotalDeductions())
                    .netPay(p.getNetPay())
                    .payrollStatus(p.getPayrollStatus().name())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionReportResponse> getCollectionReport(LocalDate from, LocalDate to, Long clientId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<InvoicePayment> payments = (clientId != null)
                ? invoicePaymentRepository.findByTenantIdAndDateRangeAndClient(tenantId, from, to, clientId)
                : invoicePaymentRepository.findByTenantIdAndDateRange(tenantId, from, to);

        return payments.stream().map(p -> CollectionReportResponse.builder()
                .paymentId(p.getId())
                .paymentDate(p.getPaymentDate())
                .amount(p.getAmount())
                .paymentMode(p.getPaymentMode().name())
                .referenceNumber(p.getReferenceNumber())
                .remarks(p.getRemarks())
                .invoiceId(p.getInvoice().getId())
                .invoiceNumber(p.getInvoice().getInvoiceNumber())
                .clientId(p.getInvoice().getClient().getId())
                .clientName(p.getInvoice().getClient().getClientName())
                .build()
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientStatementResponse getClientStatement(Long clientId, LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(clientId, tenantId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        List<Invoice> invoices = invoiceRepository.findByClientIdAndTenantIdAndIsActiveTrue(clientId, tenantId)
                .stream()
                .filter(i -> !i.getInvoiceDate().isBefore(from) && !i.getInvoiceDate().isAfter(to))
                .sorted(Comparator.comparing(Invoice::getInvoiceDate))
                .toList();

        List<InvoicePayment> payments = invoicePaymentRepository
                .findByTenantIdAndClientIdAndDateRange(tenantId, clientId, from, to);

        // Merge invoices and payments into a single timeline
        List<ClientStatementRow> allRows = new ArrayList<>();
        for (Invoice inv : invoices) {
            allRows.add(ClientStatementRow.builder()
                    .type("INVOICE")
                    .date(inv.getInvoiceDate())
                    .referenceNumber(inv.getInvoiceNumber())
                    .description("Invoice raised")
                    .debit(inv.getTotalAmount())
                    .credit(BigDecimal.ZERO)
                    .balance(BigDecimal.ZERO) // calculated below
                    .build());
        }
        for (InvoicePayment pmt : payments) {
            allRows.add(ClientStatementRow.builder()
                    .type("PAYMENT")
                    .date(pmt.getPaymentDate())
                    .referenceNumber(pmt.getReferenceNumber() != null ? pmt.getReferenceNumber() : pmt.getInvoice().getInvoiceNumber())
                    .description("Payment received via " + pmt.getPaymentMode().name())
                    .debit(BigDecimal.ZERO)
                    .credit(pmt.getAmount())
                    .balance(BigDecimal.ZERO) // calculated below
                    .build());
        }

        // Sort by date, invoices before payments on same day
        allRows.sort(Comparator.comparing(ClientStatementRow::getDate)
                .thenComparing(r -> r.getType().equals("INVOICE") ? 0 : 1));

        // Compute running balance
        BigDecimal running = BigDecimal.ZERO;
        for (ClientStatementRow row : allRows) {
            running = running.add(row.getDebit()).subtract(row.getCredit());
            row.setBalance(running);
        }

        BigDecimal totalInvoiced = invoices.stream()
                .map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = payments.stream()
                .map(InvoicePayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ClientStatementResponse.builder()
                .clientId(client.getId())
                .clientName(client.getClientName())
                .clientPhone(client.getPhone())
                .gstin(client.getGstin())
                .totalInvoiced(totalInvoiced)
                .totalPaid(totalPaid)
                .closingBalance(totalInvoiced.subtract(totalPaid))
                .rows(allRows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleTripResponse> getVehicleTripReport(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);

        // Group by vehicle
        Map<Long, List<Lr>> byVehicle = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getVehicleAllocation().getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<Lr> vehicleLrs = entry.getValue();
            Vehicle vehicle = vehicleLrs.get(0).getVehicleAllocation().getVehicle();

            BigDecimal totalAllocated = vehicleLrs.stream()
                    .map(Lr::getAllocatedWeight).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalLoaded = vehicleLrs.stream()
                    .map(Lr::getLoadedWeight).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivered = vehicleLrs.stream()
                    .map(Lr::getDeliveredWeight).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return VehicleTripResponse.builder()
                    .vehicleId(vehicle.getId())
                    .registrationNumber(vehicle.getRegistrationNumber())
                    .vehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().getName() : null)
                    .brand(vehicle.getBrand() != null ? vehicle.getBrand().getName() : null)
                    .totalTrips(vehicleLrs.size())
                    .totalAllocatedWeight(totalAllocated)
                    .totalLoadedWeight(totalLoaded)
                    .totalDeliveredWeight(totalDelivered)
                    .build();
        }).sorted(Comparator.comparingInt(VehicleTripResponse::getTotalTrips).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusResponse> getOrderStatusReport(LocalDate from, LocalDate to, String status) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByTenantIdAndDateRangeAndStatus(
                    tenantId, from, to, OrderStatus.valueOf(status));
        } else {
            orders = orderRepository.findByTenantIdAndDateRange(tenantId, from, to);
        }

        return orders.stream().map(o -> OrderStatusResponse.builder()
                .orderId(o.getId())
                .orderNumber(o.getOrderNumber())
                .orderDate(o.getOrderDate())
                .expectedDeliveryDate(o.getExpectedDeliveryDate())
                .clientId(o.getClient().getId())
                .clientName(o.getClient().getClientName())
                .fromCity(o.getSourceCity().getName())
                .fromState(o.getSourceState().getName())
                .toCity(o.getDestinationCity().getName())
                .toState(o.getDestinationState().getName())
                .materialType(o.getMaterialType().getName())
                .totalWeight(o.getTotalWeight())
                .totalFreightAmount(o.getTotalFreightAmount())
                .orderStatus(o.getOrderStatus().name())
                .orderPaymentStatus(o.getOrderPaymentStatus().name())
                .build()
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceReportResponse> getAttendanceReport(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Attendance> records = attendanceRepository.findByTenantIdAndDateRange(tenantId, from, to);

        long totalDays = ChronoUnit.DAYS.between(from, to) + 1;

        // Group by user
        Map<Long, List<Attendance>> byUser = records.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        return byUser.entrySet().stream().map(entry -> {
            List<Attendance> userRecords = entry.getValue();
            User user = userRecords.get(0).getUser();
            String roleName = user.getRoles().stream()
                    .findFirst().map(r -> r.getName().name()).orElse(null);

            int present = (int) userRecords.stream()
                    .filter(a -> "PRESENT".equals(a.getAttendanceType().getName())).count();
            int absent = (int) userRecords.stream()
                    .filter(a -> "ABSENT".equals(a.getAttendanceType().getName())).count();
            int halfDay = (int) userRecords.stream()
                    .filter(a -> "HALF_DAY".equals(a.getAttendanceType().getName())).count();
            int leave = (int) userRecords.stream()
                    .filter(a -> "LEAVE".equals(a.getAttendanceType().getName())).count();

            BigDecimal percentage = totalDays > 0
                    ? BigDecimal.valueOf((present + halfDay * 0.5) / totalDays * 100)
                    .setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return AttendanceReportResponse.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .userPhone(user.getPhone())
                    .roleName(roleName)
                    .totalDays((int) totalDays)
                    .presentDays(present)
                    .absentDays(absent)
                    .halfDays(halfDay)
                    .leaveDays(leave)
                    .attendancePercentage(percentage)
                    .build();
        }).sorted(Comparator.comparing(AttendanceReportResponse::getUserName))
                .toList();
    }

    // ─── Section A — Daily Operations ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DailyVehicleActivityResponse getDailyVehicleActivity() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        List<Lr> onRoadLrs = lrRepository.findByTenantIdAndLrStatus(tenantId, LrStatus.IN_TRANSIT);
        List<Lr> startedLrs = lrRepository.findByTenantIdAndLoadedAtDate(tenantId, today);
        List<Lr> deliveredLrs = lrRepository.findByTenantIdAndDeliveredAtDate(tenantId, today);

        Set<Long> activeLrVehicleIds = onRoadLrs.stream()
                .map(l -> l.getVehicleAllocation().getVehicle().getId())
                .collect(Collectors.toSet());

        List<Vehicle> allVehicles = vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId);
        List<DailyVehicleActivityResponse.VehicleActivityRow> idleRows = allVehicles.stream()
                .filter(v -> !activeLrVehicleIds.contains(v.getId()))
                .map(v -> DailyVehicleActivityResponse.VehicleActivityRow.builder()
                        .vehicleId(v.getId())
                        .registrationNumber(v.getRegistrationNumber())
                        .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                        .build())
                .toList();

        return DailyVehicleActivityResponse.builder()
                .onRoadCount(onRoadLrs.size())
                .startedTodayCount(startedLrs.size())
                .deliveredTodayCount(deliveredLrs.size())
                .idleCount(idleRows.size())
                .onRoad(mapToActivityRows(onRoadLrs))
                .startedToday(mapToActivityRows(startedLrs))
                .deliveredToday(mapToActivityRows(deliveredLrs))
                .idle(idleRows)
                .build();
    }

    private List<DailyVehicleActivityResponse.VehicleActivityRow> mapToActivityRows(List<Lr> lrs) {
        return lrs.stream().map(l -> {
            Vehicle v = l.getVehicleAllocation().getVehicle();
            Order o = l.getOrder();
            return DailyVehicleActivityResponse.VehicleActivityRow.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                    .clientName(o.getClient().getClientName())
                    .fromCity(o.getSourceCity().getName())
                    .toCity(o.getDestinationCity().getName())
                    .lrNumber(l.getLrNumber())
                    .loadedAt(l.getLoadedAt() != null ? l.getLoadedAt().format(DT_FMT) : null)
                    .deliveredAt(l.getDeliveredAt() != null ? l.getDeliveredAt().format(DT_FMT) : null)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocalLongTripSummaryResponse getLocalLongTripSummary() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        List<Lr> lrs = lrRepository.findByTenantIdAndLoadedAtDate(tenantId, today);

        List<LocalLongTripSummaryResponse.TripRow> trips = lrs.stream().map(l -> {
            Order o = l.getOrder();
            boolean isLocal = o.getSourceState().getId().equals(o.getDestinationState().getId());
            return LocalLongTripSummaryResponse.TripRow.builder()
                    .tripType(isLocal ? "LOCAL" : "LONG")
                    .lrNumber(l.getLrNumber())
                    .registrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                    .clientName(o.getClient().getClientName())
                    .fromCity(o.getSourceCity().getName())
                    .fromState(o.getSourceState().getName())
                    .toCity(o.getDestinationCity().getName())
                    .toState(o.getDestinationState().getName())
                    .build();
        }).toList();

        long localCount = trips.stream().filter(t -> "LOCAL".equals(t.getTripType())).count();
        return LocalLongTripSummaryResponse.builder()
                .localCount((int) localCount)
                .longDistanceCount(trips.size() - (int) localCount)
                .totalToday(trips.size())
                .trips(trips)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IdleDriverResponse> getIdleDrivers() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<User> drivers = userRepository.findByTenantIdAndRoleNames(tenantId,
                List.of(RoleName.DRIVER, RoleName.SUPERVISOR));

        List<StaffAllocationStatus> activeStatuses = List.of(StaffAllocationStatus.ALLOCATED, StaffAllocationStatus.IN_TRANSIT);

        return drivers.stream()
                .filter(u -> staffAllocationRepository.findActiveAllocationsForUser(u.getId(), activeStatuses).isEmpty())
                .map(u -> IdleDriverResponse.builder()
                        .userId(u.getId())
                        .userName(u.getName())
                        .phone(u.getPhone())
                        .roleName(u.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(null))
                        .build())
                .sorted(Comparator.comparing(IdleDriverResponse::getUserName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentExpiryAlertResponse> getDocumentExpiryAlerts(int daysAhead) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        LocalDate alertDate = today.plusDays(daysAhead);

        List<VehicleDocument> docs = vehicleDocumentRepository.findExpiringDocuments(tenantId, alertDate);
        return docs.stream().map(d -> {
            long daysUntil = ChronoUnit.DAYS.between(today, d.getExpiryDate());
            return DocumentExpiryAlertResponse.builder()
                    .vehicleId(d.getVehicle().getId())
                    .registrationNumber(d.getVehicle().getRegistrationNumber())
                    .documentType(d.getDocumentType().getName())
                    .documentNumber(d.getDocumentNumber())
                    .expiryDate(d.getExpiryDate())
                    .daysUntilExpiry(daysUntil)
                    .expired(daysUntil < 0)
                    .build();
        }).sorted(Comparator.comparingLong(DocumentExpiryAlertResponse::getDaysUntilExpiry)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TodayAttendanceSummaryResponse getTodayAttendance() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        List<Attendance> todayRecords = attendanceRepository.findByTenantIdAndAttendanceDateAndIsActiveTrue(tenantId, today);
        Map<Long, Attendance> byUser = todayRecords.stream()
                .collect(Collectors.toMap(a -> a.getUser().getId(), a -> a, (a, b) -> a));

        List<User> allUsers = userRepository.findAllByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(u -> u.getRoles().stream().noneMatch(r -> r.getName() == RoleName.SUPER_ADMIN))
                .toList();

        List<TodayAttendanceSummaryResponse.TodayAttendanceRow> rows = allUsers.stream().map(u -> {
            Attendance att = byUser.get(u.getId());
            String status = att != null ? att.getAttendanceType().getName().toUpperCase() : "NOT_MARKED";
            return TodayAttendanceSummaryResponse.TodayAttendanceRow.builder()
                    .userId(u.getId())
                    .userName(u.getName())
                    .phone(u.getPhone())
                    .roleName(u.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(null))
                    .attendanceStatus(status)
                    .build();
        }).sorted(Comparator.comparing(TodayAttendanceSummaryResponse.TodayAttendanceRow::getUserName)).toList();

        int present = (int) rows.stream().filter(r -> "PRESENT".equals(r.getAttendanceStatus())).count();
        int absent = (int) rows.stream().filter(r -> "ABSENT".equals(r.getAttendanceStatus())).count();
        int leave = (int) rows.stream().filter(r -> "LEAVE".equals(r.getAttendanceStatus())).count();
        int notMarked = (int) rows.stream().filter(r -> "NOT_MARKED".equals(r.getAttendanceStatus())).count();

        return TodayAttendanceSummaryResponse.builder()
                .presentCount(present)
                .absentCount(absent)
                .leaveCount(leave)
                .notMarkedCount(notMarked)
                .totalStaff(allUsers.size())
                .records(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DelayedTripResponse> getDelayedTrips() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        List<Lr> inTransit = lrRepository.findByTenantIdAndLrStatus(tenantId, LrStatus.IN_TRANSIT);

        return inTransit.stream()
                .filter(l -> l.getOrder().getExpectedDeliveryDate() != null
                        && l.getOrder().getExpectedDeliveryDate().isBefore(today))
                .map(l -> {
                    Order o = l.getOrder();
                    long daysDelayed = ChronoUnit.DAYS.between(o.getExpectedDeliveryDate(), today);
                    return DelayedTripResponse.builder()
                            .lrId(l.getId())
                            .lrNumber(l.getLrNumber())
                            .registrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                            .clientName(o.getClient().getClientName())
                            .fromCity(o.getSourceCity().getName())
                            .toCity(o.getDestinationCity().getName())
                            .expectedDeliveryDate(o.getExpectedDeliveryDate())
                            .daysDelayed(daysDelayed)
                            .loadedAt(l.getLoadedAt() != null ? l.getLoadedAt().format(DT_FMT) : null)
                            .build();
                })
                .sorted(Comparator.comparingLong(DelayedTripResponse::getDaysDelayed).reversed())
                .toList();
    }

    // ─── Section C — Orders & Assignments ─────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderFulfillmentRateResponse getOrderFulfillmentRate(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Order> orders = orderRepository.findByTenantIdAndDateRange(tenantId, from, to);
        int total = orders.size();
        int pending = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.PENDING).count();
        int partial = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.PARTIALLY_ASSIGNED).count();
        int fullyAssigned = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.FULLY_ASSIGNED).count();
        int inTransit = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.IN_TRANSIT).count();
        int delivered = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED).count();
        int completed = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED).count();
        int cancelled = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED).count();
        double rate = total > 0 ? (double)(delivered + completed) / total * 100 : 0;
        return OrderFulfillmentRateResponse.builder()
                .totalOrders(total).pending(pending).partiallyAssigned(partial)
                .fullyAssigned(fullyAssigned).inTransit(inTransit)
                .delivered(delivered).completed(completed).cancelled(cancelled)
                .fulfillmentRate(Math.round(rate * 10.0) / 10.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderLeadTimeResponse> getOrderLeadTime(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to).stream()
                .filter(l -> l.getDeliveredAt() != null)
                .toList();

        // Group by route (fromCity -> toCity)
        Map<String, List<Lr>> byRoute = lrs.stream().collect(Collectors.groupingBy(
                l -> l.getOrder().getSourceCity().getName() + "→" + l.getOrder().getDestinationCity().getName()));

        return byRoute.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("→");
            List<Lr> routeLrs = entry.getValue();
            List<Double> leadTimes = routeLrs.stream()
                    .map(l -> (double) ChronoUnit.DAYS.between(l.getOrder().getOrderDate(), l.getDeliveredAt().toLocalDate()))
                    .toList();
            double avg = leadTimes.stream().mapToDouble(d -> d).average().orElse(0);
            double min = leadTimes.stream().mapToDouble(d -> d).min().orElse(0);
            double max = leadTimes.stream().mapToDouble(d -> d).max().orElse(0);
            return OrderLeadTimeResponse.builder()
                    .fromCity(parts[0]).toCity(parts.length > 1 ? parts[1] : "")
                    .orderCount(routeLrs.size())
                    .avgLeadTimeDays(Math.round(avg * 10.0) / 10.0)
                    .minLeadTimeDays(min).maxLeadTimeDays(max)
                    .build();
        }).sorted(Comparator.comparingDouble(OrderLeadTimeResponse::getAvgLeadTimeDays).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnassignedVehiclesResponse> getUnassignedVehicles() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        List<Order> pending = orderRepository.findByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PENDING);
        List<Order> partial = orderRepository.findByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PARTIALLY_ASSIGNED);
        List<Order> backlog = new ArrayList<>();
        backlog.addAll(pending);
        backlog.addAll(partial);

        return backlog.stream().map(o -> {
            int vehiclesAssigned = vehicleAllocationRepository.findByTenantIdAndOrderId(tenantId, o.getId()).size();
            return UnassignedVehiclesResponse.builder()
                    .orderId(o.getId()).orderNumber(o.getOrderNumber()).orderDate(o.getOrderDate())
                    .clientName(o.getClient().getClientName())
                    .fromCity(o.getSourceCity().getName()).toCity(o.getDestinationCity().getName())
                    .materialType(o.getMaterialType().getName()).totalWeight(o.getTotalWeight())
                    .vehiclesAssigned(vehiclesAssigned).orderStatus(o.getOrderStatus().name())
                    .daysWaiting(ChronoUnit.DAYS.between(o.getOrderDate(), today))
                    .build();
        }).sorted(Comparator.comparingLong(UnassignedVehiclesResponse::getDaysWaiting).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverAssignmentHistoryResponse> getDriverAssignmentHistory(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return staffAllocationRepository.findByTenantIdAndDateRange(tenantId, from, to).stream().map(sa -> {
            User driver = sa.getUser();
            Order o = sa.getOrder();
            Vehicle v = sa.getVehicleAllocation().getVehicle();
            return DriverAssignmentHistoryResponse.builder()
                    .allocationId(sa.getId())
                    .driverName(driver.getName()).driverPhone(driver.getPhone())
                    .roleName(driver.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(null))
                    .registrationNumber(v.getRegistrationNumber())
                    .orderNumber(o.getOrderNumber())
                    .clientName(o.getClient().getClientName())
                    .fromCity(o.getSourceCity().getName()).toCity(o.getDestinationCity().getName())
                    .expectedStartDate(sa.getExpectedStartDate()).expectedEndDate(sa.getExpectedEndDate())
                    .allocationStatus(sa.getAllocationStatus().name())
                    .build();
        }).toList();
    }

    // ─── Section D — Trips & LRs ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TripInProgressResponse> getTripsInProgress() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        return lrRepository.findByTenantIdAndLrStatus(tenantId, LrStatus.IN_TRANSIT).stream().map(l -> {
            Order o = l.getOrder();
            long daysInTransit = l.getLoadedAt() != null
                    ? ChronoUnit.DAYS.between(l.getLoadedAt().toLocalDate(), today) : 0;
            return TripInProgressResponse.builder()
                    .lrId(l.getId()).lrNumber(l.getLrNumber())
                    .registrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                    .clientName(o.getClient().getClientName())
                    .fromCity(o.getSourceCity().getName()).toCity(o.getDestinationCity().getName())
                    .loadedAt(l.getLoadedAt() != null ? l.getLoadedAt().format(DT_FMT) : null)
                    .expectedDeliveryDate(o.getExpectedDeliveryDate())
                    .daysInTransit(daysInTransit).loadedWeight(l.getLoadedWeight())
                    .build();
        }).sorted(Comparator.comparingLong(TripInProgressResponse::getDaysInTransit).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LrStatusFunnelResponse getLrStatusFunnel(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);
        int created   = (int) lrs.stream().filter(l -> l.getLrStatus() == LrStatus.CREATED).count();
        int inTransit = (int) lrs.stream().filter(l -> l.getLrStatus() == LrStatus.IN_TRANSIT).count();
        int delivered = (int) lrs.stream().filter(l -> l.getLrStatus() == LrStatus.DELIVERED).count();
        int cancelled = (int) lrs.stream().filter(l -> l.getLrStatus() == LrStatus.CANCELLED).count();
        return LrStatusFunnelResponse.builder()
                .created(created).inTransit(inTransit)
                .delivered(delivered).cancelled(cancelled).total(lrs.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnbilledLrResponse> getUnbilledLrs() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        Set<Long> invoicedLrIds = new HashSet<>(invoiceLrRepository.findActiveLrIds(tenantId));
        return lrRepository.findByTenantIdAndLrStatus(tenantId, LrStatus.DELIVERED).stream()
                .filter(l -> !invoicedLrIds.contains(l.getId()))
                .map(l -> {
                    Order o = l.getOrder();
                    long days = l.getDeliveredAt() != null
                            ? ChronoUnit.DAYS.between(l.getDeliveredAt().toLocalDate(), today) : 0;
                    return UnbilledLrResponse.builder()
                            .lrId(l.getId()).lrNumber(l.getLrNumber())
                            .registrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                            .clientName(o.getClient().getClientName())
                            .fromCity(o.getSourceCity().getName()).toCity(o.getDestinationCity().getName())
                            .deliveredAt(l.getDeliveredAt() != null ? l.getDeliveredAt().format(DT_FMT) : null)
                            .deliveredWeight(l.getDeliveredWeight())
                            .daysSinceDelivery(days)
                            .build();
                }).sorted(Comparator.comparingLong(UnbilledLrResponse::getDaysSinceDelivery).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceTurnaroundResponse> getInvoiceTurnaround(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<InvoiceLr> invoiceLrs = invoiceLrRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(il -> {
                    LocalDate invDate = il.getInvoice().getInvoiceDate();
                    return !invDate.isBefore(from) && !invDate.isAfter(to);
                })
                .filter(il -> il.getLr().getDeliveredAt() != null)
                .toList();

        // Group by client
        Map<String, List<InvoiceLr>> byClient = invoiceLrs.stream()
                .collect(Collectors.groupingBy(il -> il.getOrder().getClient().getClientName()));

        return byClient.entrySet().stream().map(entry -> {
            List<Long> turnarounds = entry.getValue().stream()
                    .map(il -> ChronoUnit.DAYS.between(il.getLr().getDeliveredAt().toLocalDate(), il.getInvoice().getInvoiceDate()))
                    .toList();
            double avg = turnarounds.stream().mapToLong(d -> d).average().orElse(0);
            long max = turnarounds.stream().mapToLong(d -> d).max().orElse(0);
            return InvoiceTurnaroundResponse.builder()
                    .clientName(entry.getKey())
                    .lrCount(entry.getValue().size())
                    .avgTurnaroundDays(Math.round(avg * 10.0) / 10.0)
                    .maxTurnaroundDays(max)
                    .build();
        }).sorted(Comparator.comparingDouble(InvoiceTurnaroundResponse::getAvgTurnaroundDays).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDurationResponse> getTripDurationAnalysis(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to).stream()
                .filter(l -> l.getLoadedAt() != null && l.getDeliveredAt() != null)
                .toList();

        Map<String, List<Lr>> byRoute = lrs.stream().collect(Collectors.groupingBy(
                l -> l.getOrder().getSourceCity().getName() + "→" + l.getOrder().getDestinationCity().getName()));

        return byRoute.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("→");
            List<Double> durations = entry.getValue().stream()
                    .map(l -> (double) java.time.Duration.between(l.getLoadedAt(), l.getDeliveredAt()).toMinutes() / 60.0)
                    .toList();
            double avg = durations.stream().mapToDouble(d -> d).average().orElse(0);
            double min = durations.stream().mapToDouble(d -> d).min().orElse(0);
            double max = durations.stream().mapToDouble(d -> d).max().orElse(0);
            return TripDurationResponse.builder()
                    .fromCity(parts[0]).toCity(parts.length > 1 ? parts[1] : "")
                    .tripCount(entry.getValue().size())
                    .avgDurationHours(Math.round(avg * 10.0) / 10.0)
                    .minDurationHours(Math.round(min * 10.0) / 10.0)
                    .maxDurationHours(Math.round(max * 10.0) / 10.0)
                    .build();
        }).sorted(Comparator.comparingDouble(TripDurationResponse::getAvgDurationHours).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeightVarianceReportResponse> getWeightVarianceReport(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to).stream()
                .filter(l -> l.getLoadedWeight() != null && l.getDeliveredWeight() != null)
                .toList();

        Map<Long, List<Lr>> byClient = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getOrder().getClient().getId()));

        return byClient.entrySet().stream().map(entry -> {
            List<Lr> clientLrs = entry.getValue();
            String clientName = clientLrs.get(0).getOrder().getClient().getClientName();
            BigDecimal totalLoaded = clientLrs.stream().map(Lr::getLoadedWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivered = clientLrs.stream().map(Lr::getDeliveredWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal variance = totalLoaded.subtract(totalDelivered);
            double pct = totalLoaded.compareTo(BigDecimal.ZERO) > 0
                    ? variance.divide(totalLoaded, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
            return WeightVarianceReportResponse.builder()
                    .clientId(entry.getKey()).clientName(clientName)
                    .lrCount(clientLrs.size())
                    .totalLoadedWeight(totalLoaded).totalDeliveredWeight(totalDelivered)
                    .totalVariance(variance)
                    .avgVariancePct(Math.round(pct * 10.0) / 10.0)
                    .build();
        }).sorted(Comparator.comparing(WeightVarianceReportResponse::getTotalVariance).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverloadingIncidentResponse> getOverloadingIncidents(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return lrRepository.findOverloadedByTenantIdAndDateRange(tenantId, from, to).stream().map(l -> {
            Order o = l.getOrder();
            return OverloadingIncidentResponse.builder()
                    .lrId(l.getId()).lrNumber(l.getLrNumber())
                    .registrationNumber(l.getVehicleAllocation().getVehicle().getRegistrationNumber())
                    .clientName(o.getClient().getClientName())
                    .fromCity(o.getSourceCity().getName()).toCity(o.getDestinationCity().getName())
                    .lrDate(l.getLrDate())
                    .allocatedWeight(l.getAllocatedWeight())
                    .loadedWeight(l.getLoadedWeight())
                    .overloadWeight(l.getOverloadWeight())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdersBacklogResponse> getOrdersBacklog() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        List<Order> pending = orderRepository.findByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PENDING);
        List<Order> partial = orderRepository.findByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PARTIALLY_ASSIGNED);

        List<Order> backlog = new ArrayList<>();
        backlog.addAll(pending);
        backlog.addAll(partial);

        return backlog.stream().map(o -> OrdersBacklogResponse.builder()
                .orderId(o.getId())
                .orderNumber(o.getOrderNumber())
                .orderDate(o.getOrderDate())
                .clientName(o.getClient().getClientName())
                .fromCity(o.getSourceCity().getName())
                .toCity(o.getDestinationCity().getName())
                .materialType(o.getMaterialType().getName())
                .totalWeight(o.getTotalWeight())
                .orderStatus(o.getOrderStatus().name())
                .daysWaiting(ChronoUnit.DAYS.between(o.getOrderDate(), today))
                .build()
        ).sorted(Comparator.comparingLong(OrdersBacklogResponse::getDaysWaiting).reversed()).toList();
    }

    // ─── Section E — Vehicle Performance ──────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<VehicleRevenueResponse> getVehicleRevenue(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<Long, List<Lr>> byVehicle = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getVehicleAllocation().getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<Lr> vehicleLrs = entry.getValue();
            Vehicle v = vehicleLrs.get(0).getVehicleAllocation().getVehicle();

            BigDecimal revenue = vehicleLrs.stream().map(l -> {
                Order o = l.getOrder();
                if (o.getFreightRate() == null) return BigDecimal.ZERO;
                return switch (o.getFreightRateType()) {
                    case PER_TON -> {
                        BigDecimal weight = o.getBillingOn() == BillingOn.DELIVERED_WEIGHT
                                ? (l.getDeliveredWeight() != null ? l.getDeliveredWeight() : BigDecimal.ZERO)
                                : (l.getLoadedWeight() != null ? l.getLoadedWeight() : BigDecimal.ZERO);
                        yield o.getFreightRate().multiply(weight);
                    }
                    case PER_TRIP, PER_KM -> o.getFreightRate();
                };
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalLoaded = vehicleLrs.stream()
                    .map(l -> l.getLoadedWeight() != null ? l.getLoadedWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return VehicleRevenueResponse.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                    .tripCount(vehicleLrs.size())
                    .totalLoadedTons(totalLoaded)
                    .estimatedRevenue(revenue.setScale(2, RoundingMode.HALF_UP))
                    .build();
        }).sorted(Comparator.comparing(VehicleRevenueResponse::getEstimatedRevenue).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleIdleDaysResponse> getVehicleIdleDays(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        int totalDays = (int) ChronoUnit.DAYS.between(from, to) + 1;

        List<Vehicle> vehicles = vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId);
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<Long, List<Lr>> byVehicle = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getVehicleAllocation().getVehicle().getId()));

        return vehicles.stream().map(v -> {
            List<Lr> vehicleLrs = byVehicle.getOrDefault(v.getId(), List.of());
            // Active days = distinct days the vehicle had a trip loaded
            Set<LocalDate> activeDates = vehicleLrs.stream()
                    .filter(l -> l.getLoadedAt() != null)
                    .map(l -> l.getLoadedAt().toLocalDate())
                    .filter(d -> !d.isBefore(from) && !d.isAfter(to))
                    .collect(Collectors.toSet());
            int activeDays = activeDates.size();
            int idleDays = totalDays - activeDays;
            double idlePct = totalDays > 0 ? Math.round((double) idleDays / totalDays * 1000.0) / 10.0 : 0;

            return VehicleIdleDaysResponse.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                    .totalDays(totalDays)
                    .activeDays(activeDays)
                    .idleDays(idleDays)
                    .idlePct(idlePct)
                    .build();
        }).sorted(Comparator.comparingInt(VehicleIdleDaysResponse::getIdleDays).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleTripCountResponse> getVehicleTripCount(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<Long, List<Lr>> byVehicle = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getVehicleAllocation().getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<Lr> vehicleLrs = entry.getValue();
            Vehicle v = vehicleLrs.get(0).getVehicleAllocation().getVehicle();
            BigDecimal totalLoaded = vehicleLrs.stream()
                    .map(l -> l.getLoadedWeight() != null ? l.getLoadedWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivered = vehicleLrs.stream()
                    .map(l -> l.getDeliveredWeight() != null ? l.getDeliveredWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return VehicleTripCountResponse.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                    .tripCount(vehicleLrs.size())
                    .totalLoadedTons(totalLoaded)
                    .totalDeliveredTons(totalDelivered)
                    .build();
        }).sorted(Comparator.comparingInt(VehicleTripCountResponse::getTripCount).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakdownFrequencyResponse> getBreakdownFrequency(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<VehicleBreakdown> breakdowns = vehicleBreakdownRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<Long, List<VehicleBreakdown>> byVehicle = breakdowns.stream()
                .collect(Collectors.groupingBy(b -> b.getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<VehicleBreakdown> vBreakdowns = entry.getValue();
            Vehicle v = vBreakdowns.get(0).getVehicle();
            List<String> types = vBreakdowns.stream()
                    .map(b -> b.getBreakdownType().name())
                    .distinct().toList();
            String lastDate = vBreakdowns.get(0).getBreakdownDate() != null
                    ? vBreakdowns.get(0).getBreakdownDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null;

            return BreakdownFrequencyResponse.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                    .breakdownCount(vBreakdowns.size())
                    .breakdownTypes(types)
                    .lastBreakdownDate(lastDate)
                    .build();
        }).sorted(Comparator.comparingInt(BreakdownFrequencyResponse::getBreakdownCount).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleServiceCostResponse> getVehicleServiceCost(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<VehicleService> services = vehicleServiceRepository
                .findByTenantIdAndServiceDateBetweenAndIsActiveTrue(tenantId, from, to);

        Map<Long, List<VehicleService>> byVehicle = services.stream()
                .collect(Collectors.groupingBy(s -> s.getVehicle().getId()));

        return byVehicle.entrySet().stream().map(entry -> {
            List<VehicleService> vServices = entry.getValue();
            Vehicle v = vServices.get(0).getVehicle();
            BigDecimal totalCost = vServices.stream()
                    .map(s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String lastDate = vServices.stream()
                    .max(Comparator.comparing(s -> s.getServiceDate() != null ? s.getServiceDate() : LocalDate.MIN))
                    .map(s -> s.getServiceDate() != null ? s.getServiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null)
                    .orElse(null);

            return VehicleServiceCostResponse.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                    .serviceCount(vServices.size())
                    .totalServiceCost(totalCost)
                    .lastServiceDate(lastDate)
                    .build();
        }).sorted(Comparator.comparing(VehicleServiceCostResponse::getTotalServiceCost).reversed()).toList();
    }

    // ─── Section F — Driver & Staff Performance ────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DriverPerformanceResponse> getDriverPerformance(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<OrderStaffAllocation> allocations = staffAllocationRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<Long, List<OrderStaffAllocation>> byUser = allocations.stream()
                .collect(Collectors.groupingBy(sa -> sa.getUser().getId()));

        return byUser.entrySet().stream().map(entry -> {
            List<OrderStaffAllocation> userAllocs = entry.getValue();
            User driver = userAllocs.get(0).getUser();

            // Collect LRs from the vehicle allocations (one allocation may have multiple LRs)
            List<Lr> driverLrs = userAllocs.stream()
                    .flatMap(sa -> lrRepository.findAllByVehicleAllocationId(sa.getVehicleAllocation().getId()).stream())
                    .distinct()
                    .toList();

            BigDecimal totalLoaded = driverLrs.stream()
                    .map(l -> l.getLoadedWeight() != null ? l.getLoadedWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivered = driverLrs.stream()
                    .map(l -> l.getDeliveredWeight() != null ? l.getDeliveredWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return DriverPerformanceResponse.builder()
                    .userId(driver.getId())
                    .driverName(driver.getName())
                    .phone(driver.getPhone())
                    .roleName(driver.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(null))
                    .tripCount(driverLrs.size())
                    .totalLoadedTons(totalLoaded)
                    .totalDeliveredTons(totalDelivered)
                    .build();
        }).sorted(Comparator.comparingInt(DriverPerformanceResponse::getTripCount).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceGapsResponse> getAttendanceGaps(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Attendance> records = attendanceRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<Long, Set<LocalDate>> markedDays = records.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getUser().getId(),
                        Collectors.mapping(Attendance::getAttendanceDate, Collectors.toSet())
                ));

        // All calendar days in range
        List<LocalDate> allDays = from.datesUntil(to.plusDays(1)).toList();

        List<User> staffUsers = userRepository.findAllByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(u -> u.getRoles().stream().noneMatch(r -> r.getName() == RoleName.SUPER_ADMIN))
                .toList();

        return staffUsers.stream().map(u -> {
            Set<LocalDate> marked = markedDays.getOrDefault(u.getId(), Set.of());
            List<LocalDate> gaps = allDays.stream().filter(d -> !marked.contains(d)).toList();
            return AttendanceGapsResponse.builder()
                    .userId(u.getId())
                    .userName(u.getName())
                    .roleName(u.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(null))
                    .totalGapDays(gaps.size())
                    .gapDates(gaps)
                    .build();
        }).filter(r -> r.getTotalGapDays() > 0)
          .sorted(Comparator.comparingInt(AttendanceGapsResponse::getTotalGapDays).reversed())
          .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceTrendResponse> getAttendanceTrend(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Attendance> records = attendanceRepository.findByTenantIdAndDateRange(tenantId, from, to);
        int totalStaff = (int) userRepository.findAllByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(u -> u.getRoles().stream().noneMatch(r -> r.getName() == RoleName.SUPER_ADMIN))
                .count();

        Map<LocalDate, List<Attendance>> byDate = records.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate));

        return from.datesUntil(to.plusDays(1)).map(date -> {
            List<Attendance> dayRecords = byDate.getOrDefault(date, List.of());
            int present = (int) dayRecords.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getAttendanceType().getName())).count();
            int absent  = (int) dayRecords.stream().filter(a -> "ABSENT".equalsIgnoreCase(a.getAttendanceType().getName())).count();
            int leave   = (int) dayRecords.stream().filter(a -> "LEAVE".equalsIgnoreCase(a.getAttendanceType().getName())).count();
            int notMarked = totalStaff - dayRecords.size();
            return AttendanceTrendResponse.builder()
                    .date(date)
                    .presentCount(present)
                    .absentCount(absent)
                    .leaveCount(leave)
                    .notMarkedCount(Math.max(notMarked, 0))
                    .totalStaff(totalStaff)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceCalendarResponse getAttendanceCalendar(int year, int month) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        int daysInMonth = ym.lengthOfMonth();

        List<Attendance> records = attendanceRepository.findByTenantIdAndDateRange(tenantId, from, to);

        // Map: userId -> (day -> status)
        Map<Long, Map<Integer, String>> userDayStatus = new HashMap<>();
        for (Attendance a : records) {
            userDayStatus
                    .computeIfAbsent(a.getUser().getId(), k -> new HashMap<>())
                    .put(a.getAttendanceDate().getDayOfMonth(), a.getAttendanceType().getName());
        }

        List<User> staffUsers = userRepository.findAllByTenantIdAndIsActiveTrue(tenantId).stream()
                .filter(u -> u.getRoles().stream().noneMatch(r -> r.getName() == RoleName.SUPER_ADMIN))
                .sorted(Comparator.comparing(User::getName))
                .toList();

        List<AttendanceCalendarResponse.UserCalendarRow> rows = staffUsers.stream().map(u -> {
            Map<Integer, String> dayMap = userDayStatus.getOrDefault(u.getId(), Map.of());
            return AttendanceCalendarResponse.UserCalendarRow.builder()
                    .userId(u.getId())
                    .userName(u.getName())
                    .roleName(u.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(null))
                    .dailyStatus(dayMap)
                    .build();
        }).toList();

        return AttendanceCalendarResponse.builder()
                .year(year)
                .month(month)
                .daysInMonth(daysInMonth)
                .users(rows)
                .build();
    }

    // ─── Section G — Financial Intelligence ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public InvoiceAgingResponse getInvoiceAging() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        List<Invoice> outstanding = invoiceRepository.findOutstandingInvoices(tenantId);

        List<InvoiceAgingResponse.AgingRow> rows = outstanding.stream().map(inv -> {
            int age = (int) ChronoUnit.DAYS.between(inv.getInvoiceDate(), today);
            return InvoiceAgingResponse.AgingRow.builder()
                    .invoiceId(inv.getId())
                    .invoiceNumber(inv.getInvoiceNumber())
                    .clientName(inv.getClient().getClientName())
                    .invoiceDate(inv.getInvoiceDate())
                    .balanceDue(inv.getBalanceDue())
                    .ageInDays(age)
                    .build();
        }).sorted(Comparator.comparingInt(InvoiceAgingResponse.AgingRow::getAgeInDays).reversed()).toList();

        List<InvoiceAgingResponse.AgingRow> b0  = rows.stream().filter(r -> r.getAgeInDays() <= 30).toList();
        List<InvoiceAgingResponse.AgingRow> b31  = rows.stream().filter(r -> r.getAgeInDays() > 30 && r.getAgeInDays() <= 60).toList();
        List<InvoiceAgingResponse.AgingRow> b61  = rows.stream().filter(r -> r.getAgeInDays() > 60 && r.getAgeInDays() <= 90).toList();
        List<InvoiceAgingResponse.AgingRow> b90  = rows.stream().filter(r -> r.getAgeInDays() > 90).toList();

        BigDecimal total = rows.stream().map(InvoiceAgingResponse.AgingRow::getBalanceDue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return InvoiceAgingResponse.builder()
                .totalOutstanding(rows.size())
                .totalAmount(total)
                .bucket0to30(bucket(b0))
                .bucket31to60(bucket(b31))
                .bucket61to90(bucket(b61))
                .bucket90plus(bucket(b90))
                .build();
    }

    private InvoiceAgingResponse.AgingBucket bucket(List<InvoiceAgingResponse.AgingRow> rows) {
        BigDecimal amt = rows.stream().map(InvoiceAgingResponse.AgingRow::getBalanceDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        return InvoiceAgingResponse.AgingBucket.builder().count(rows.size()).amount(amt).invoices(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueTrendResponse> getRevenueTrend() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        LocalDate sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1);

        List<Invoice> invoices = invoiceRepository.findByTenantIdAndDateRange(tenantId, sixMonthsAgo, today);

        Map<String, List<Invoice>> byMonth = invoices.stream()
                .collect(Collectors.groupingBy(inv -> inv.getInvoiceDate().getYear() + "-" + String.format("%02d", inv.getInvoiceDate().getMonthValue())));

        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        return byMonth.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("-");
            int yr = Integer.parseInt(parts[0]);
            int mo = Integer.parseInt(parts[1]);
            List<Invoice> monthInvoices = entry.getValue();
            BigDecimal subtotal = monthInvoices.stream().map(i -> i.getSubtotal() != null ? i.getSubtotal() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tax = monthInvoices.stream().map(i -> i.getTaxAmount() != null ? i.getTaxAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = monthInvoices.stream().map(i -> i.getTotalAmount() != null ? i.getTotalAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            return RevenueTrendResponse.builder()
                    .period(monthNames[mo - 1] + " " + yr)
                    .year(yr).month(mo)
                    .invoiceCount(monthInvoices.size())
                    .subtotal(subtotal).taxAmount(tax).totalRevenue(total)
                    .build();
        }).sorted(Comparator.comparingInt(RevenueTrendResponse::getYear).thenComparingInt(RevenueTrendResponse::getMonth)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteProfitabilityResponse> getRouteProfitability(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);

        // Load all charges for tenant, index by lrId
        Map<Long, List<LrCharge>> chargesByLr = lrChargeRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream().collect(Collectors.groupingBy(c -> c.getLr().getId()));

        Map<String, List<Lr>> byRoute = lrs.stream().collect(Collectors.groupingBy(
                l -> l.getOrder().getSourceCity().getName() + "→" + l.getOrder().getDestinationCity().getName()));

        return byRoute.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("→");
            List<Lr> routeLrs = entry.getValue();

            BigDecimal revenue = routeLrs.stream().map(l -> {
                Order o = l.getOrder();
                if (o.getFreightRate() == null) return BigDecimal.ZERO;
                return switch (o.getFreightRateType()) {
                    case PER_TON -> {
                        BigDecimal w = o.getBillingOn() == BillingOn.DELIVERED_WEIGHT
                                ? (l.getDeliveredWeight() != null ? l.getDeliveredWeight() : BigDecimal.ZERO)
                                : (l.getLoadedWeight() != null ? l.getLoadedWeight() : BigDecimal.ZERO);
                        yield o.getFreightRate().multiply(w);
                    }
                    case PER_TRIP, PER_KM -> o.getFreightRate();
                };
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal charges = routeLrs.stream()
                    .flatMap(l -> chargesByLr.getOrDefault(l.getId(), List.of()).stream())
                    .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal netProfit = revenue.subtract(charges);
            double margin = revenue.compareTo(BigDecimal.ZERO) > 0
                    ? netProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0;

            return RouteProfitabilityResponse.builder()
                    .fromCity(parts[0]).toCity(parts.length > 1 ? parts[1] : "")
                    .tripCount(routeLrs.size())
                    .totalRevenue(revenue.setScale(2, RoundingMode.HALF_UP))
                    .totalCharges(charges.setScale(2, RoundingMode.HALF_UP))
                    .netProfit(netProfit.setScale(2, RoundingMode.HALF_UP))
                    .profitMarginPct(Math.round(margin * 10.0) / 10.0)
                    .build();
        }).sorted(Comparator.comparing(RouteProfitabilityResponse::getNetProfit).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GstSummaryResponse> getGstSummary(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Invoice> invoices = invoiceRepository.findByTenantIdAndDateRange(tenantId, from, to);
        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        Map<String, List<Invoice>> byMonth = invoices.stream()
                .collect(Collectors.groupingBy(inv -> inv.getInvoiceDate().getYear() + "-" + String.format("%02d", inv.getInvoiceDate().getMonthValue())));

        return byMonth.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("-");
            int yr = Integer.parseInt(parts[0]);
            int mo = Integer.parseInt(parts[1]);
            List<Invoice> monthInvoices = entry.getValue();

            BigDecimal subtotal = monthInvoices.stream().map(i -> i.getSubtotal() != null ? i.getSubtotal() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cgst = monthInvoices.stream().map(i -> i.getCgstAmount() != null ? i.getCgstAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sgst = monthInvoices.stream().map(i -> i.getSgstAmount() != null ? i.getSgstAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = monthInvoices.stream().map(i -> i.getTotalAmount() != null ? i.getTotalAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

            return GstSummaryResponse.builder()
                    .period(monthNames[mo - 1] + " " + yr)
                    .year(yr).month(mo)
                    .invoiceCount(monthInvoices.size())
                    .subtotal(subtotal)
                    .cgstAmount(cgst)
                    .sgstAmount(sgst)
                    .totalTax(cgst.add(sgst))
                    .totalAmount(total)
                    .build();
        }).sorted(Comparator.comparingInt(GstSummaryResponse::getYear).thenComparingInt(GstSummaryResponse::getMonth)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditNoteSummaryResponse> getCreditNotesSummary(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return invoiceCreditNoteRepository.findByTenantIdAndDateRange(tenantId, from, to).stream().map(cn ->
                CreditNoteSummaryResponse.builder()
                        .creditNoteId(cn.getId())
                        .creditNoteNumber(cn.getCreditNoteNumber())
                        .clientName(cn.getClient().getClientName())
                        .creditNoteDate(cn.getCreditNoteDate())
                        .amount(cn.getAmount())
                        .reason(cn.getReason())
                        .status(cn.getCreditNoteStatus().name())
                        .invoiceNumber(cn.getInvoice() != null ? cn.getInvoice().getInvoiceNumber() : null)
                        .build()
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientPendingBillingResponse> getClientPendingBilling() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        Set<Long> invoicedLrIds = new HashSet<>(invoiceLrRepository.findActiveLrIds(tenantId));

        List<Lr> unbilled = lrRepository.findByTenantIdAndLrStatus(tenantId, LrStatus.DELIVERED).stream()
                .filter(l -> !invoicedLrIds.contains(l.getId()))
                .toList();

        Map<Long, List<Lr>> byClient = unbilled.stream()
                .collect(Collectors.groupingBy(l -> l.getOrder().getClient().getId()));

        return byClient.entrySet().stream().map(entry -> {
            List<Lr> clientLrs = entry.getValue();
            String clientName = clientLrs.get(0).getOrder().getClient().getClientName();
            BigDecimal totalTons = clientLrs.stream()
                    .map(l -> l.getDeliveredWeight() != null ? l.getDeliveredWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDate oldest = clientLrs.stream()
                    .filter(l -> l.getDeliveredAt() != null)
                    .map(l -> l.getDeliveredAt().toLocalDate())
                    .min(Comparator.naturalOrder()).orElse(null);
            long daysPending = oldest != null ? ChronoUnit.DAYS.between(oldest, today) : 0;

            return ClientPendingBillingResponse.builder()
                    .clientId(entry.getKey())
                    .clientName(clientName)
                    .pendingLrCount(clientLrs.size())
                    .totalDeliveredTons(totalTons)
                    .oldestDeliveryDate(oldest)
                    .daysPending(daysPending)
                    .build();
        }).sorted(Comparator.comparingLong(ClientPendingBillingResponse::getDaysPending).reversed()).toList();
    }

    // ─── Section H — Monthly Business Intelligence ─────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TopClientResponse> getTopClients(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Order> orders = orderRepository.findByTenantIdAndDateRange(tenantId, from, to);
        List<Invoice> invoices = invoiceRepository.findByTenantIdAndDateRange(tenantId, from, to);

        // Revenue per client from invoices
        Map<Long, BigDecimal> revenueByClient = invoices.stream()
                .collect(Collectors.groupingBy(i -> i.getClient().getId(),
                        Collectors.reducing(BigDecimal.ZERO,
                                i -> i.getTotalAmount() != null ? i.getTotalAmount() : BigDecimal.ZERO,
                                BigDecimal::add)));

        // Orders grouped by client
        Map<Long, List<Order>> ordersByClient = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getClient().getId()));

        // LRs for the period
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, from, to);
        Map<Long, List<Lr>> lrsByClient = lrs.stream()
                .collect(Collectors.groupingBy(l -> l.getOrder().getClient().getId()));

        return ordersByClient.entrySet().stream().map(entry -> {
            Long clientId = entry.getKey();
            List<Order> clientOrders = entry.getValue();
            String clientName = clientOrders.get(0).getClient().getClientName();
            List<Lr> clientLrs = lrsByClient.getOrDefault(clientId, List.of());
            BigDecimal totalTons = clientLrs.stream()
                    .map(l -> l.getLoadedWeight() != null ? l.getLoadedWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return TopClientResponse.builder()
                    .clientId(clientId)
                    .clientName(clientName)
                    .orderCount(clientOrders.size())
                    .tripCount(clientLrs.size())
                    .totalTonnage(totalTons)
                    .totalRevenue(revenueByClient.getOrDefault(clientId, BigDecimal.ZERO))
                    .build();
        }).sorted(Comparator.comparing(TopClientResponse::getTotalRevenue).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopMaterialResponse> getTopMaterials(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Order> orders = orderRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<String, List<Order>> byMaterial = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getMaterialType().getName()));

        BigDecimal grandTotal = orders.stream()
                .map(o -> o.getTotalWeight() != null ? o.getTotalWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return byMaterial.entrySet().stream().map(entry -> {
            BigDecimal weight = entry.getValue().stream()
                    .map(o -> o.getTotalWeight() != null ? o.getTotalWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? weight.divide(grandTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
            return TopMaterialResponse.builder()
                    .materialType(entry.getKey())
                    .orderCount(entry.getValue().size())
                    .totalWeight(weight)
                    .pct(Math.round(pct * 10.0) / 10.0)
                    .build();
        }).sorted(Comparator.comparing(TopMaterialResponse::getTotalWeight).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopRouteResponse> getTopRoutes(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Order> orders = orderRepository.findByTenantIdAndDateRange(tenantId, from, to);

        Map<String, List<Order>> byRoute = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getSourceCity().getName() + "→" + o.getDestinationCity().getName()));

        return byRoute.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("→");
            List<Order> routeOrders = entry.getValue();
            BigDecimal weight = routeOrders.stream()
                    .map(o -> o.getTotalWeight() != null ? o.getTotalWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return TopRouteResponse.builder()
                    .fromCity(parts[0]).toCity(parts.length > 1 ? parts[1] : "")
                    .orderCount(routeOrders.size())
                    .totalWeight(weight)
                    .build();
        }).sorted(Comparator.comparingInt(TopRouteResponse::getOrderCount).reversed()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OnTimeDeliveryResponse getOnTimeDeliveryRate(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Lr> delivered = lrRepository.findByTenantIdAndLrStatusAndDateRange(tenantId, LrStatus.DELIVERED, from, to);

        int total = delivered.size();
        int onTime = (int) delivered.stream().filter(l -> {
            LocalDate expected = l.getOrder().getExpectedDeliveryDate();
            LocalDate actualDate = l.getDeliveredAt() != null ? l.getDeliveredAt().toLocalDate() : null;
            return expected != null && actualDate != null && !actualDate.isAfter(expected);
        }).count();
        int delayed = total - onTime;
        double rate = total > 0 ? Math.round((double) onTime / total * 1000.0) / 10.0 : 0;

        return OnTimeDeliveryResponse.builder()
                .totalDelivered(total).onTime(onTime).delayed(delayed).onTimeRate(rate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCancellationRateResponse getOrderCancellationRate(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<Order> orders = orderRepository.findByTenantIdAndDateRange(tenantId, from, to);
        int total = orders.size();
        int cancelled = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED).count();
        int active = total - cancelled;
        double rate = total > 0 ? Math.round((double) cancelled / total * 1000.0) / 10.0 : 0;

        return OrderCancellationRateResponse.builder()
                .totalOrders(total).cancelled(cancelled).active(active).cancellationRate(rate)
                .build();
    }

    // ─── Section I — Inventory Reports ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<StockLevelResponse> getStockLevels() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return sparePartsInventoryRepository.findByTenantIdOrderBySparePartNameAsc(tenantId).stream()
                .map(inv -> StockLevelResponse.builder()
                        .partId(inv.getSparePart().getId())
                        .partName(inv.getSparePart().getName())
                        .partNumber(inv.getSparePart().getPartNumber())
                        .category(inv.getSparePart().getCategory())
                        .unit(inv.getSparePart().getUnit())
                        .currentStock(inv.getQuantity())
                        .minStockLevel(inv.getSparePart().getMinStockLevel())
                        .isLowStock(inv.getQuantity() <= inv.getSparePart().getMinStockLevel())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovement(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return sparePartsTransactionRepository
                .findByTenantIdAndCreatedAtBetweenOrderByCreatedAtDesc(tenantId,
                        from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .map(tx -> StockMovementResponse.builder()
                        .transactionId(tx.getId())
                        .date(tx.getCreatedAt().toLocalDate())
                        .partId(tx.getSparePart().getId())
                        .partName(tx.getSparePart().getName())
                        .partNumber(tx.getSparePart().getPartNumber())
                        .category(tx.getSparePart().getCategory())
                        .transactionType(tx.getTransactionType().name())
                        .quantity(tx.getQuantity())
                        .unitCost(tx.getUnitCost())
                        .totalCost(tx.getTotalCost())
                        .supplierName(tx.getSupplierName())
                        .notes(tx.getNotes())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiclePartConsumptionResponse> getPartsByVehicle(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<ServicePart> parts = servicePartRepository
                .findByService_TenantIdAndService_ServiceDateBetweenAndStatus(tenantId, from, to, ServicePartStatus.APPROVED);

        // Group by vehicleId → partId → total quantity
        Map<Long, Map<Long, int[]>> grouped = new LinkedHashMap<>();
        Map<Long, Vehicle> vehicleMap = new HashMap<>();
        Map<Long, com.feros.api.entity.master.SparePart> partMap = new HashMap<>();

        for (ServicePart sp : parts) {
            Vehicle v = sp.getService().getVehicle();
            com.feros.api.entity.master.SparePart p = sp.getSparePart();
            vehicleMap.put(v.getId(), v);
            partMap.put(p.getId(), p);
            grouped.computeIfAbsent(v.getId(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(p.getId(), k -> new int[1])[0] +=
                    (sp.getQuantityApproved() != null ? sp.getQuantityApproved() : 0);
        }

        List<VehiclePartConsumptionResponse> result = new ArrayList<>();
        grouped.forEach((vehicleId, partTotals) -> {
            Vehicle v = vehicleMap.get(vehicleId);
            partTotals.forEach((partId, qty) -> {
                com.feros.api.entity.master.SparePart p = partMap.get(partId);
                result.add(VehiclePartConsumptionResponse.builder()
                        .vehicleId(vehicleId)
                        .regNo(v.getRegistrationNumber())
                        .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                        .partName(p.getName())
                        .partNumber(p.getPartNumber())
                        .category(p.getCategory())
                        .totalQuantity(qty[0])
                        .build());
            });
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartConsumptionByTypeResponse> getPartsByType(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<ServicePart> parts = servicePartRepository
                .findByService_TenantIdAndService_ServiceDateBetweenAndStatus(tenantId, from, to, ServicePartStatus.APPROVED);

        Map<Long, PartConsumptionByTypeResponse> map = new LinkedHashMap<>();
        for (ServicePart sp : parts) {
            com.feros.api.entity.master.SparePart p = sp.getSparePart();
            PartConsumptionByTypeResponse row = map.computeIfAbsent(p.getId(), k ->
                    PartConsumptionByTypeResponse.builder()
                            .partId(p.getId()).partName(p.getName()).partNumber(p.getPartNumber())
                            .category(p.getCategory()).unit(p.getUnit()).totalQuantity(0).serviceCount(0)
                            .build());
            row.setTotalQuantity(row.getTotalQuantity() + (sp.getQuantityApproved() != null ? sp.getQuantityApproved() : 0));
            row.setServiceCount(row.getServiceCount() + 1);
        }
        return new ArrayList<>(map.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCostBreakdownResponse> getServiceCostBreakdown(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<VehicleService> services = vehicleServiceRepository
                .findByTenantIdAndServiceDateBetweenAndIsActiveTrue(tenantId, from, to);
        List<ServicePart> approvedParts = servicePartRepository
                .findByService_TenantIdAndService_ServiceDateBetweenAndStatus(tenantId, from, to, ServicePartStatus.APPROVED);

        Map<Long, Long> partsCountByService = approvedParts.stream()
                .collect(Collectors.groupingBy(sp -> sp.getService().getId(), Collectors.counting()));

        return services.stream()
                .sorted(Comparator.comparing(VehicleService::getServiceDate).reversed())
                .map(svc -> ServiceCostBreakdownResponse.builder()
                        .serviceId(svc.getId())
                        .vehicleId(svc.getVehicle().getId())
                        .regNo(svc.getVehicle().getRegistrationNumber())
                        .vehicleType(svc.getVehicle().getVehicleType() != null ? svc.getVehicle().getVehicleType().getName() : null)
                        .serviceDate(svc.getServiceDate())
                        .serviceType(svc.getServiceType().name())
                        .status(svc.getStatus().name())
                        .totalCost(svc.getTotalCost())
                        .partsUsedCount(partsCountByService.getOrDefault(svc.getId(), 0L).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Section J — Tyre Reports ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TyresByVehicleResponse> getTyresByVehicle() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        List<VehicleTyreFitting> fittings = vehicleTyreFittingRepository.findAllActiveFittingsByTenantId(tenantId);

        Map<Long, List<VehicleTyreFitting>> byVehicle = fittings.stream()
                .collect(Collectors.groupingBy(f -> f.getVehicle().getId()));

        return byVehicle.entrySet().stream()
                .map(e -> {
                    Vehicle v = e.getValue().get(0).getVehicle();
                    List<TyresByVehicleResponse.TyreFittingItem> items = e.getValue().stream()
                            .map(f -> TyresByVehicleResponse.TyreFittingItem.builder()
                                    .fittingId(f.getId())
                                    .tyreId(f.getTyre().getId())
                                    .serialNumber(f.getTyre().getSerialNumber())
                                    .brand(f.getTyre().getBrand())
                                    .size(f.getTyre().getSize())
                                    .tyreType(f.getTyre().getTyreType().name())
                                    .positionCode(f.getPosition().getPositionCode())
                                    .fittedDate(f.getFittedDate())
                                    .fittedAtKm(f.getFittedAtKm())
                                    .kmDriven(f.getKmDriven())
                                    .build())
                            .collect(Collectors.toList());
                    return TyresByVehicleResponse.builder()
                            .vehicleId(v.getId())
                            .regNo(v.getRegistrationNumber())
                            .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                            .activeTyreCount(items.size())
                            .tyres(items)
                            .build();
                })
                .sorted(Comparator.comparing(TyresByVehicleResponse::getRegNo))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KmPerTyreResponse> getKmPerTyre() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return vehicleTyreFittingRepository.findAllFittingsByTenantId(tenantId).stream()
                .map(f -> KmPerTyreResponse.builder()
                        .fittingId(f.getId())
                        .tyreId(f.getTyre().getId())
                        .serialNumber(f.getTyre().getSerialNumber())
                        .brand(f.getTyre().getBrand())
                        .size(f.getTyre().getSize())
                        .tyreType(f.getTyre().getTyreType().name())
                        .vehicleId(f.getVehicle().getId())
                        .vehicleRegNo(f.getVehicle().getRegistrationNumber())
                        .positionCode(f.getPosition().getPositionCode())
                        .fittedDate(f.getFittedDate())
                        .fittedAtKm(f.getFittedAtKm())
                        .removedAtKm(f.getRemovedAtKm())
                        .kmDriven(f.getKmDriven())
                        .active(f.getRemovedAtKm() == null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TyreReplacementProjectionResponse> getTyreReplacementProjection() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return vehicleTyreFittingRepository.findAllActiveFittingsByTenantId(tenantId).stream()
                .filter(f -> f.getTyre().getMaxLifetimeKm() != null
                        && f.getTyre().getMaxLifetimeKm().compareTo(BigDecimal.ZERO) > 0)
                .map(f -> {
                    BigDecimal total = f.getTyre().getTotalLifetimeKm() != null
                            ? f.getTyre().getTotalLifetimeKm() : BigDecimal.ZERO;
                    BigDecimal max = f.getTyre().getMaxLifetimeKm();
                    BigDecimal remaining = max.subtract(total);
                    double pct = remaining.divide(max, 4, RoundingMode.HALF_UP).doubleValue();
                    String urgency = pct < 0.10 ? "HIGH" : pct < 0.30 ? "MEDIUM" : "LOW";
                    return TyreReplacementProjectionResponse.builder()
                            .tyreId(f.getTyre().getId())
                            .serialNumber(f.getTyre().getSerialNumber())
                            .brand(f.getTyre().getBrand())
                            .size(f.getTyre().getSize())
                            .vehicleId(f.getVehicle().getId())
                            .vehicleRegNo(f.getVehicle().getRegistrationNumber())
                            .positionCode(f.getPosition().getPositionCode())
                            .fittedDate(f.getFittedDate())
                            .totalLifetimeKm(total)
                            .maxLifetimeKm(max)
                            .remainingKm(remaining.max(BigDecimal.ZERO))
                            .urgency(urgency)
                            .build();
                })
                .sorted(Comparator.comparing(TyreReplacementProjectionResponse::getRemainingKm))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TyreCostPerKmResponse> getTyreCostPerKm() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tyreRepository.findByTenantIdAndIsActiveTrueOrderByIdDesc(tenantId).stream()
                .filter(t -> t.getPurchaseCost() != null
                        && t.getTotalLifetimeKm() != null
                        && t.getTotalLifetimeKm().compareTo(BigDecimal.ZERO) > 0)
                .map(t -> {
                    BigDecimal costPerKm = t.getPurchaseCost().divide(t.getTotalLifetimeKm(), 4, RoundingMode.HALF_UP);
                    return TyreCostPerKmResponse.builder()
                            .tyreId(t.getId())
                            .serialNumber(t.getSerialNumber())
                            .brand(t.getBrand())
                            .size(t.getSize())
                            .tyreType(t.getTyreType().name())
                            .purchaseCost(t.getPurchaseCost())
                            .totalLifetimeKm(t.getTotalLifetimeKm())
                            .costPerKm(costPerKm)
                            .build();
                })
                .sorted(Comparator.comparing(TyreCostPerKmResponse::getCostPerKm).reversed())
                .collect(Collectors.toList());
    }
}
