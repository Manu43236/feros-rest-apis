package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.response.DashboardResponse;
import com.feros.api.dto.response.DriverDashboardResponse;
import com.feros.api.dto.response.ExpiryAlertResponse;
import com.feros.api.dto.response.SupervisorDashboardResponse;
import com.feros.api.entity.Attendance;
import com.feros.api.entity.Lr;
import com.feros.api.entity.StaffDocument;
import com.feros.api.entity.User;
import com.feros.api.entity.VehicleDocument;
import com.feros.api.entity.VehicleMeterReading;
import com.feros.api.enums.MeterReadingType;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.enums.BreakdownStatus;
import com.feros.api.enums.InvoiceStatus;
import com.feros.api.enums.LrStatus;
import com.feros.api.enums.OrderStatus;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.repository.*;
import com.feros.api.service.DashboardService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final OrderStaffAllocationRepository staffAllocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final VehicleRepository vehicleRepository;
    private final AttendanceRepository attendanceRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final LrRepository lrRepository;
    private final VehicleBreakdownRepository vehicleBreakdownRepository;
    private final NotificationRepository notificationRepository;
    private final VehicleMeterReadingRepository vehicleMeterReadingRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final LeaseDriverAssignmentLogRepository leaseDriverAssignmentLogRepository;
    private final LeaseVehicleSessionRepository leaseVehicleSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        // Orders
        long totalOrders = orderRepository.countByTenantIdAndIsActiveTrue(tenantId);
        DashboardResponse.OrderSummary orderSummary = DashboardResponse.OrderSummary.builder()
                .total(totalOrders)
                .pending(orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PENDING))
                .partiallyAssigned(orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PARTIALLY_ASSIGNED))
                .fullyAssigned(orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.FULLY_ASSIGNED))
                .inTransit(orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.IN_TRANSIT))
                .partiallyDelivered(orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PARTIALLY_DELIVERED))
                .delivered(orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.DELIVERED))
                .cancelled(orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.CANCELLED))
                .build();

        // Vehicles
        long totalVehicles = vehicleRepository.countByTenantIdAndIsActiveTrue(tenantId);
        DashboardResponse.VehicleSummary vehicleSummary = DashboardResponse.VehicleSummary.builder()
                .total(totalVehicles)
                .available(vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.AVAILABLE))
                .assigned(vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.ASSIGNED))
                .onTrip(vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.ON_TRIP))
                .underMaintenance(vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.IN_REPAIR))
                .breakdown(vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.BREAKDOWN))
                .inactive(vehicleRepository.countByTenantIdAndIsActiveFalse(tenantId))
                .build();

        // Invoices
        DashboardResponse.InvoiceSummary invoiceSummary = DashboardResponse.InvoiceSummary.builder()
                .draft(invoiceRepository.countByTenantIdAndInvoiceStatusAndIsActiveTrue(tenantId, InvoiceStatus.DRAFT))
                .sent(invoiceRepository.countByTenantIdAndInvoiceStatusAndIsActiveTrue(tenantId, InvoiceStatus.SENT))
                .partiallyPaid(invoiceRepository.countByTenantIdAndInvoiceStatusAndIsActiveTrue(tenantId, InvoiceStatus.PARTIALLY_PAID))
                .overdue(invoiceRepository.countByTenantIdAndInvoiceStatusAndIsActiveTrue(tenantId, InvoiceStatus.OVERDUE))
                .paid(invoiceRepository.countByTenantIdAndInvoiceStatusAndIsActiveTrue(tenantId, InvoiceStatus.PAID))
                .totalOutstanding(invoiceRepository.sumOutstandingBalanceByTenantId(tenantId))
                .totalRevenue(invoiceRepository.sumTotalRevenueByTenantId(tenantId))
                .build();

        // Today's Attendance
        List<Attendance> todayList = attendanceRepository.findByTenantIdAndAttendanceDateAndIsActiveTrue(tenantId, today);
        long present = 0, absent = 0, halfDay = 0, onLeave = 0;
        for (Attendance a : todayList) {
            String type = a.getAttendanceType().getName().toLowerCase();
            if (type.contains("present")) present++;
            else if (type.contains("absent")) absent++;
            else if (type.contains("half")) halfDay++;
            else if (type.contains("leave")) onLeave++;
        }
        DashboardResponse.AttendanceSummary attendanceSummary = DashboardResponse.AttendanceSummary.builder()
                .date(today)
                .present(present)
                .absent(absent)
                .halfDay(halfDay)
                .onLeave(onLeave)
                .total(todayList.size())
                .build();

        return DashboardResponse.builder()
                .orders(orderSummary)
                .vehicles(vehicleSummary)
                .invoices(invoiceSummary)
                .todayAttendance(attendanceSummary)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpiryAlertResponse getExpiryAlerts(int days) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        LocalDate alertDate = today.plusDays(days);

        List<ExpiryAlertResponse.VehicleAlert> vehicleAlerts = new ArrayList<>();

        // Vehicle documents table
        List<VehicleDocument> vehicleDocs = vehicleDocumentRepository.findExpiringDocuments(tenantId, alertDate);
        for (VehicleDocument vd : vehicleDocs) {
            long daysLeft = ChronoUnit.DAYS.between(today, vd.getExpiryDate());
            vehicleAlerts.add(ExpiryAlertResponse.VehicleAlert.builder()
                    .vehicleId(vd.getVehicle().getId())
                    .registrationNumber(vd.getVehicle().getRegistrationNumber())
                    .alertType("DOCUMENT")
                    .documentName(vd.getDocumentType().getName())
                    .expiryDate(vd.getExpiryDate())
                    .daysLeft(daysLeft)
                    .expired(daysLeft < 0)
                    .build());
        }

        // Staff documents
        List<StaffDocument> staffDocs = staffDocumentRepository.findExpiringDocuments(tenantId, alertDate);
        List<ExpiryAlertResponse.StaffDocumentAlert> staffAlerts = staffDocs.stream().map(sd -> {
            long daysLeft = ChronoUnit.DAYS.between(today, sd.getExpiryDate());
            return ExpiryAlertResponse.StaffDocumentAlert.builder()
                    .userId(sd.getUser().getId())
                    .userName(sd.getUser().getName())
                    .documentType(sd.getDocumentType().getName())
                    .documentNumber(sd.getDocumentNumber())
                    .expiryDate(sd.getExpiryDate())
                    .daysLeft(daysLeft)
                    .expired(daysLeft < 0)
                    .build();
        }).toList();

        return ExpiryAlertResponse.builder()
                .vehicleAlerts(vehicleAlerts)
                .staffDocumentAlerts(staffAlerts)
                .totalAlerts(vehicleAlerts.size() + staffAlerts.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DriverDashboardResponse getDriverDashboard() {
        Long userId = SecurityUtil.getCurrentUserId();
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        List<Lr> myLrs = lrRepository.findByTenantIdAndDriverUserId(tenantId, userId);

        int totalTrips = myLrs.size();
        int pendingTrips = (int) myLrs.stream()
                .filter(lr -> lr.getLrStatus() == LrStatus.CREATED
                        || lr.getLrStatus() == LrStatus.WEIGHT_LOADED
                        || lr.getLrStatus() == LrStatus.IN_TRANSIT)
                .count();

        boolean attendanceMarked = attendanceRepository
                .existsByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrueAndApprovalStatusNot(
                        userId, tenantId, today, AttendanceApprovalStatus.REJECTED);

        java.time.LocalDateTime markedOutAt = null;
        boolean canUndoOut = false;
        String dutyLabel = null;
        var todayAtt = attendanceRepository.findByUserIdAndTenantIdAndAttendanceDateAndIsActiveTrue(userId, tenantId, today);
        if (todayAtt.isPresent() && todayAtt.get().getMarkedOutAt() != null) {
            markedOutAt = todayAtt.get().getMarkedOutAt();
            canUndoOut  = java.time.Duration.between(markedOutAt, com.feros.api.util.TimeUtil.nowIst()).toMinutes() <= 10;
            long totalMinutes = java.time.Duration.between(todayAtt.get().getMarkedAt(), markedOutAt).toMinutes();
            double hours = Math.round(totalMinutes / 6.0) / 10.0;
            dutyLabel = hours + " hrs";
        }

        int unreadCount = (int) notificationRepository.countUnread(tenantId, userId);

        boolean attendanceEnforced = tenantSettingsRepository.findByTenantId(tenantId)
                .map(s -> Boolean.TRUE.equals(s.getAttendanceEnforced()))
                .orElse(false);

        // Active trip — currently IN_TRANSIT
        DriverDashboardResponse.UpcomingTrip activeTrip = myLrs.stream()
                .filter(lr -> lr.getLrStatus() == LrStatus.IN_TRANSIT)
                .findFirst()
                .map(lr -> {
                        Long allocationId = lr.getVehicleAllocation().getId();
                        boolean hasBreakdown = vehicleBreakdownRepository
                                .findFirstByVehicleAllocationIdAndIsActiveTrueOrderByCreatedAtDesc(allocationId)
                                .map(b -> b.getStatus() == BreakdownStatus.REPORTED)
                                .orElse(false);
                        return DriverDashboardResponse.UpcomingTrip.builder()
                                .lrId(lr.getId())
                                .orderId(lr.getOrder().getId())
                                .vehicleAllocationId(allocationId)
                                .lrNumber(lr.getLrNumber())
                                .lrStatus(lr.getLrStatus().name())
                                .clientName(lr.getOrder().getClient().getClientName())
                                .fromCity(lr.getOrder().getSourceCity() != null
                                        ? lr.getOrder().getSourceCity().getName() : "—")
                                .toCity(lr.getOrder().getDestinationCity() != null
                                        ? lr.getOrder().getDestinationCity().getName() : "—")
                                .vehicleNumber(lr.getVehicleAllocation().getVehicle().getRegistrationNumber())
                                .currentVehicleOdometer(vehicleMeterReadingRepository
                                        .findLatestTripEndByVehicle(lr.getVehicleAllocation().getVehicle().getId())
                                        .stream().findFirst().map(VehicleMeterReading::getReadingKm)
                                        .orElse(lr.getVehicleAllocation().getVehicle().getCurrentOdometerReading()))
                                .startOdometer(vehicleMeterReadingRepository
                                        .findTopByLrIdAndReadingTypeAndIsActiveTrueOrderByRecordedAtAsc(
                                                lr.getId(), MeterReadingType.TRIP_START)
                                        .map(VehicleMeterReading::getReadingKm).orElse(null))
                                .startOdometerRecordedAt(vehicleMeterReadingRepository
                                        .findTopByLrIdAndReadingTypeAndIsActiveTrueOrderByRecordedAtAsc(
                                                lr.getId(), MeterReadingType.TRIP_START)
                                        .map(VehicleMeterReading::getRecordedAt).orElse(null))
                                .loadedWeight(lr.getLoadedWeight())
                                .expectedLoadDate(lr.getVehicleAllocation().getExpectedLoadDate())
                                .expectedDeliveryDate(lr.getVehicleAllocation().getExpectedDeliveryDate())
                                .startedByName(lr.getStartedBy() != null ? lr.getStartedBy().getName() : null)
                                .startedByRole(lr.getStartedBy() != null
                                        ? lr.getStartedBy().getRoles().stream()
                                                .findFirst().map(r -> r.getName().name()).orElse(null) : null)
                                .hasActiveBreakdown(hasBreakdown)
                                .build();
                })
                .orElse(null);

        List<DriverDashboardResponse.UpcomingTrip> upcoming = myLrs.stream()
                .filter(lr -> lr.getLrStatus() == LrStatus.CREATED
                        || lr.getLrStatus() == LrStatus.WEIGHT_LOADED)
                .sorted(java.util.Comparator.comparing(
                        lr -> lr.getVehicleAllocation().getExpectedLoadDate() != null
                                ? lr.getVehicleAllocation().getExpectedLoadDate()
                                : LocalDate.MAX))
                .map(lr -> DriverDashboardResponse.UpcomingTrip.builder()
                        .lrId(lr.getId())
                        .lrNumber(lr.getLrNumber())
                        .lrStatus(lr.getLrStatus().name())
                        .clientName(lr.getOrder().getClient().getClientName())
                        .fromCity(lr.getOrder().getSourceCity() != null
                                ? lr.getOrder().getSourceCity().getName() : "—")
                        .toCity(lr.getOrder().getDestinationCity() != null
                                ? lr.getOrder().getDestinationCity().getName() : "—")
                        .vehicleNumber(lr.getVehicleAllocation().getVehicle().getRegistrationNumber())
                        .currentVehicleOdometer(vehicleMeterReadingRepository
                                        .findLatestTripEndByVehicle(lr.getVehicleAllocation().getVehicle().getId())
                                        .stream().findFirst().map(VehicleMeterReading::getReadingKm)
                                        .orElse(lr.getVehicleAllocation().getVehicle().getCurrentOdometerReading()))
                        .expectedLoadDate(lr.getVehicleAllocation().getExpectedLoadDate())
                        .expectedDeliveryDate(lr.getVehicleAllocation().getExpectedDeliveryDate())
                        .build())
                .toList();

        // ── Assigned order (staff allocated, no LR yet) ──────────────────────
        DriverDashboardResponse.AssignedOrder assignedOrder = null;
        if (activeTrip == null && upcoming.isEmpty()) {
            List<com.feros.api.entity.OrderStaffAllocation> activeAllocations =
                    staffAllocationRepository.findActiveAllocationsForUser(
                            userId, List.of(StaffAllocationStatus.ALLOCATED));
            for (com.feros.api.entity.OrderStaffAllocation sa : activeAllocations) {
                boolean hasLr = lrRepository.existsByVehicleAllocationId(sa.getVehicleAllocation().getId());
                if (!hasLr) {
                    com.feros.api.entity.OrderVehicleAllocation va = sa.getVehicleAllocation();
                    com.feros.api.entity.Order order = sa.getOrder();
                    assignedOrder = DriverDashboardResponse.AssignedOrder.builder()
                            .vehicleAllocationId(va.getId())
                            .orderId(order.getId())
                            .vehicleNumber(va.getVehicle().getRegistrationNumber())
                            .clientName(order.getClient().getClientName())
                            .fromCity(order.getSourceCity() != null ? order.getSourceCity().getName() : "—")
                            .toCity(order.getDestinationCity() != null ? order.getDestinationCity().getName() : "—")
                            .expectedLoadDate(va.getExpectedLoadDate())
                            .expectedDeliveryDate(va.getExpectedDeliveryDate())
                            .build();
                    break;
                }
            }
        }

        // ── Vehicle-level assignment (no order) ───────────────────────────────
        DriverDashboardResponse.AssignedVehicle assignedVehicle = null;
        if (activeTrip == null && upcoming.isEmpty() && assignedOrder == null) {
            var vehicleOpt = vehicleRepository.findAssignedVehiclesByStaffUserId(userId).stream().findFirst();
            if (vehicleOpt.isPresent()) {
                com.feros.api.entity.Vehicle v = vehicleOpt.get();
                assignedVehicle = DriverDashboardResponse.AssignedVehicle.builder()
                        .vehicleId(v.getId())
                        .vehicleNumber(v.getRegistrationNumber())
                        .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                        .build();
            }
        }

        // ── Active lease assignment ───────────────────────────────────────────
        DriverDashboardResponse.ActiveLease activeLease = null;
        if (activeTrip == null && upcoming.isEmpty() && assignedOrder == null) {
            activeLease = staffProfileRepository.findByUserId(userId).flatMap(sp ->
                leaseDriverAssignmentLogRepository.findActiveByDriverStaffId(sp.getId(), tenantId)
            ).map(log -> {
                var va = log.getLeaseVehicleAssignment();
                var lease = va.getLease();
                var activeSession = leaseVehicleSessionRepository.findByAssignmentIdAndIsActiveTrue(va.getId()).orElse(null);
                return DriverDashboardResponse.ActiveLease.builder()
                        .leaseId(lease.getId())
                        .assignmentId(va.getId())
                        .leaseNumber(lease.getLeaseNumber())
                        .clientName(lease.getClient().getClientName())
                        .vehicleNumber(va.getVehicle().getRegistrationNumber())
                        .divisionName(va.getDivisionName())
                        .lastOdometer(va.getVehicle().getCurrentOdometerReading())
                        .activeSessionId(activeSession != null ? activeSession.getId() : null)
                        .sessionStartTime(activeSession != null ? activeSession.getStartTime() : null)
                        .build();
            }).orElse(null);
        }

        return DriverDashboardResponse.builder()
                .totalTrips(totalTrips)
                .pendingTrips(pendingTrips)
                .attendanceMarked(attendanceMarked)
                .attendanceEnforced(attendanceEnforced)
                .markedOutAt(markedOutAt)
                .canUndoOut(canUndoOut)
                .dutyLabel(dutyLabel)
                .unreadNotifications(unreadCount)
                .activeTrip(activeTrip)
                .upcomingTrips(upcoming)
                .assignedVehicle(assignedVehicle)
                .assignedOrder(assignedOrder)
                .activeLease(activeLease)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SupervisorDashboardResponse getSupervisorDashboard() {
        Long userId   = SecurityUtil.getCurrentUserId();
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();

        // ── Orders ───────────────────────────────────────────────────────────
        int orderTotal     = (int) orderRepository.countByTenantIdAndIsActiveTrue(tenantId);
        int orderPending   = (int) (
                orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PENDING) +
                orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PARTIALLY_ASSIGNED));
        int orderActive    = (int) (
                orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.FULLY_ASSIGNED) +
                orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.IN_TRANSIT) +
                orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.PARTIALLY_DELIVERED));
        int orderCompleted = (int) orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.COMPLETED);
        int orderDelivered = (int) orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.DELIVERED);
        int orderCancelled = (int) orderRepository.countByTenantIdAndOrderStatusAndIsActiveTrue(tenantId, OrderStatus.CANCELLED);

        // ── Vehicles ─────────────────────────────────────────────────────────
        int vehicleTotal     = (int) vehicleRepository.countByTenantIdAndIsActiveTrue(tenantId);
        int vehicleAvailable = (int) vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.AVAILABLE);
        int vehicleOnTrip    = (int) (
                vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.ON_TRIP) +
                vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.ASSIGNED));
        int vehicleBreakdown = (int) (
                vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.BREAKDOWN) +
                vehicleRepository.countByTenantIdAndIsActiveTrueAndStatusType(tenantId, VehicleStatusType.IN_REPAIR));
        int vehicleInactive  = (int) vehicleRepository.countByTenantIdAndIsActiveFalse(tenantId);

        // ── Staff — Drivers & Cleaners ────────────────────────────────────────
        List<User> allStaff = userRepository.findByTenantIdAndRoleNames(
                tenantId, List.of(RoleName.DRIVER, RoleName.CLEANER));

        List<User> drivers  = allStaff.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()) &&
                        u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DRIVER))
                .toList();
        List<User> cleaners = allStaff.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()) &&
                        u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.CLEANER))
                .toList();

        int driversOnTrip  = (int) staffAllocationRepository
                .countDistinctUsersByTenantIdAndRoleAndStatus(tenantId, RoleName.DRIVER, StaffAllocationStatus.IN_TRANSIT);
        int cleanersOnTrip = (int) staffAllocationRepository
                .countDistinctUsersByTenantIdAndRoleAndStatus(tenantId, RoleName.CLEANER, StaffAllocationStatus.IN_TRANSIT);

        // ── Today's Attendance ────────────────────────────────────────────────
        List<Attendance> todayAll = attendanceRepository
                .findByTenantIdAndAttendanceDateAndIsActiveTrue(tenantId, today);

        Set<Long> driverIds  = drivers.stream().map(User::getId).collect(Collectors.toSet());
        Set<Long> cleanerIds = cleaners.stream().map(User::getId).collect(Collectors.toSet());

        int attTotal = 0, attPresent = 0, attAbsent = 0, attHalfDay = 0, attWeeklyOff = 0;
        int driverPresent = 0, cleanerPresent = 0;
        for (Attendance a : todayAll) {
            Long uid  = a.getUser().getId();
            if (!driverIds.contains(uid) && !cleanerIds.contains(uid)) continue;
            String t = a.getAttendanceType().getName().toUpperCase();
            attTotal++;
            if (t.contains("PRESENT") && !t.contains("HALF")) {
                attPresent++;
                if (driverIds.contains(uid)) driverPresent++; else cleanerPresent++;
            } else if (t.contains("ABSENT")) {
                attAbsent++;
            } else if (t.contains("HALF")) {
                attHalfDay++;
            } else if (t.contains("WEEK") || t.contains("OFF")) {
                attWeeklyOff++;
            }
        }

        // ── LRs ──────────────────────────────────────────────────────────────
        List<Lr> allLrs = lrRepository.findByTenantIdAndIsActiveTrue(tenantId);
        int lrCreated = 0, lrLoaded = 0, lrInTransit = 0, lrDelivered = 0, lrCancelled = 0;
        for (Lr lr : allLrs) {
            switch (lr.getLrStatus()) {
                case CREATED       -> lrCreated++;
                case WEIGHT_LOADED -> lrLoaded++;
                case IN_TRANSIT    -> lrInTransit++;
                case DELIVERED     -> lrDelivered++;
                case CANCELLED     -> lrCancelled++;
            }
        }

        // ── Notifications ─────────────────────────────────────────────────────
        int unreadCount = (int) notificationRepository.countUnread(tenantId, userId);

        return SupervisorDashboardResponse.builder()
                .orders(SupervisorDashboardResponse.OrderSummary.builder()
                        .total(orderTotal).pending(orderPending).active(orderActive)
                        .completed(orderCompleted).delivered(orderDelivered).cancelled(orderCancelled).build())
                .vehicles(SupervisorDashboardResponse.VehicleSummary.builder()
                        .total(vehicleTotal).available(vehicleAvailable).onTrip(vehicleOnTrip)
                        .breakdown(vehicleBreakdown).inactive(vehicleInactive).build())
                .drivers(SupervisorDashboardResponse.StaffSummary.builder()
                        .total(drivers.size()).onTrip(driversOnTrip)
                        .available(Math.max(0, drivers.size() - driversOnTrip))
                        .todayPresent(driverPresent).build())
                .cleaners(SupervisorDashboardResponse.StaffSummary.builder()
                        .total(cleaners.size()).onTrip(cleanersOnTrip)
                        .available(Math.max(0, cleaners.size() - cleanersOnTrip))
                        .todayPresent(cleanerPresent).build())
                .lrs(SupervisorDashboardResponse.LrSummary.builder()
                        .total(allLrs.size()).created(lrCreated).loaded(lrLoaded)
                        .inTransit(lrInTransit).delivered(lrDelivered).cancelled(lrCancelled).build())
                .attendance(SupervisorDashboardResponse.AttendanceSummary.builder()
                        .total(attTotal).present(attPresent).absent(attAbsent)
                        .halfDay(attHalfDay).weeklyOff(attWeeklyOff).build())
                .unreadNotifications(unreadCount)
                .build();
    }

}
