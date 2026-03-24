package com.feros.api.service.impl;

import com.feros.api.dto.response.*;
import com.feros.api.entity.*;
import com.feros.api.enums.OrderStatus;
import com.feros.api.repository.*;
import com.feros.api.service.ReportsService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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

        LocalDate today = LocalDate.now();
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
}
