package com.feros.api.service.impl;

import com.feros.api.dto.response.report.*;
import com.feros.api.entity.*;
import com.feros.api.enums.LrStatus;
import com.feros.api.repository.*;
import com.feros.api.service.ReportService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final LrRepository lrRepository;
    private final VehicleFuelLogRepository fuelLogRepository;
    private final VehicleMeterReadingRepository meterReadingRepository;
    private final VehicleBreakdownRepository breakdownRepository;
    private final VehicleDocumentRepository documentRepository;
    private final VehicleServiceRepository vehicleServiceRepository;

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

    // ── 2. Vehicle Utilization ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<VehicleUtilizationRow> getVehicleUtilization(LocalDate startDate, LocalDate endDate) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        LocalDate today = TimeUtil.today();
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        List<Vehicle> vehicles = vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId);
        List<Lr> lrs = lrRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);

        Map<Long, List<Lr>> lrsByVehicle = lrs.stream()
                .collect(Collectors.groupingBy(lr -> lr.getVehicleAllocation().getVehicle().getId()));

        return vehicles.stream().map(v -> {
            List<Lr> vehicleLrs = lrsByVehicle.getOrDefault(v.getId(), List.of());

            long daysOnTrip = vehicleLrs.stream().mapToLong(lr -> {
                if (lr.getLoadedAt() == null) return 0L;
                LocalDateTime end = (lr.getLrStatus() == LrStatus.DELIVERED && lr.getDeliveredAt() != null)
                        ? lr.getDeliveredAt()
                        : today.atTime(LocalTime.MAX);
                return Math.max(0L, ChronoUnit.DAYS.between(lr.getLoadedAt().toLocalDate(), end.toLocalDate()) + 1);
            }).sum();
            daysOnTrip = Math.min(daysOnTrip, totalDays);

            LocalDate lastTripDate = vehicleLrs.stream()
                    .map(Lr::getLrDate)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            double utilizationPercent = totalDays > 0
                    ? Math.min(100.0, BigDecimal.valueOf((double) daysOnTrip / totalDays * 100)
                            .setScale(1, RoundingMode.HALF_UP).doubleValue())
                    : 0.0;

            return VehicleUtilizationRow.builder()
                    .vehicleId(v.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleType(v.getVehicleType() != null ? v.getVehicleType().getName() : "—")
                    .totalTrips(vehicleLrs.size())
                    .daysOnTrip(daysOnTrip)
                    .totalDaysInPeriod(totalDays)
                    .utilizationPercent(utilizationPercent)
                    .lastTripDate(lastTripDate)
                    .build();
        }).toList();
    }

    // ── 3. Fuel & Mileage ─────────────────────────────────────────────────────

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
}
