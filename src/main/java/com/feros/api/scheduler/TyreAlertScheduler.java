package com.feros.api.scheduler;

import com.feros.api.util.TimeUtil;
import com.feros.api.entity.*;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.TireStatus;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TyreAlertScheduler {

    private final TireRepository tireRepository;
    private final VehicleTireFittingRepository fittingRepository;
    private final VehicleMeterReadingRepository meterReadingRepository;
    private final TenantRepository tenantRepository;
    private final NotificationService notificationService;

    private static final List<RoleName> TYRE_ALERT_ROLES =
            Arrays.asList(RoleName.ADMIN, RoleName.OFFICE_STAFF, RoleName.SUPERVISOR);

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void runTyreAlerts() {
        log.info("TyreAlertScheduler started");
        List<Tenant> tenants = tenantRepository.findAll();
        for (Tenant tenant : tenants) {
            try {
                checkExpiryDateAlerts(tenant);
                checkKmLifeAlerts(tenant);
                checkRotationDueAlerts(tenant);
            } catch (Exception e) {
                log.error("Tyre alert error for tenant {}: {}", tenant.getId(), e.getMessage());
            }
        }
        log.info("TyreAlertScheduler completed");
    }

    // ── 1. Expiry Date Alerts ─────────────────────────────────────────────────
    // Alert every day for 15 days before expiry
    private void checkExpiryDateAlerts(Tenant tenant) {
        List<Tire> tires = tireRepository.findByTenantIdAndIsActiveTrueOrderByIdDesc(tenant.getId());
        LocalDate today = TimeUtil.today();

        for (Tire tire : tires) {
            if (tire.getExpiryDate() == null) continue;
            if (tire.getStatus() == TireStatus.SCRAPPED || tire.getStatus() == TireStatus.DISPOSED) continue;

            long daysLeft = today.until(tire.getExpiryDate()).getDays();
            if (daysLeft < 0) {
                // Already expired
                notificationService.sendToRoles(tenant, TYRE_ALERT_ROLES,
                        NotificationType.TYRE_EXPIRY,
                        "Tyre Expired",
                        tire.getSerialNumber() + " (" + tire.getBrand() + " " + tire.getSize() + ") has expired. Please replace immediately.");
            } else if (daysLeft <= 15) {
                notificationService.sendToRoles(tenant, TYRE_ALERT_ROLES,
                        NotificationType.TYRE_EXPIRY,
                        "Tyre Expiring Soon",
                        tire.getSerialNumber() + " (" + tire.getBrand() + " " + tire.getSize() + ") expires in " + daysLeft + " day(s) on " + tire.getExpiryDate() + ".");
            }
        }
    }

    // ── 2. KM Life Alerts ─────────────────────────────────────────────────────
    // Alert from 85% of maxLifetimeKm, every 2000 km thereafter
    private void checkKmLifeAlerts(Tenant tenant) {
        // Get all active fittings for this tenant
        List<Tire> fittedTires = tireRepository.findByTenantIdAndIsActiveTrueOrderByIdDesc(tenant.getId())
                .stream().filter(t -> t.getStatus() == TireStatus.FITTED && t.getMaxLifetimeKm() != null).toList();

        for (Tire tire : fittedTires) {
            BigDecimal maxKm = tire.getMaxLifetimeKm();
            BigDecimal threshold85 = maxKm.multiply(BigDecimal.valueOf(0.85));

            // Get current fitting
            fittingRepository.findCurrentFittingForTire(tire.getId()).ifPresent(fitting -> {
                // Get vehicle's latest odometer
                List<VehicleMeterReading> readings = meterReadingRepository
                        .findTopByVehicleOrderByReadingKmDesc(fitting.getVehicle().getId());
                if (readings.isEmpty()) return;

                BigDecimal latestOdometer = readings.get(0).getReadingKm();
                BigDecimal kmSinceFitting = latestOdometer.subtract(fitting.getFittedAtKm());
                if (kmSinceFitting.compareTo(BigDecimal.ZERO) < 0) return;

                BigDecimal totalKmOnTyre = tire.getTotalLifetimeKm().add(kmSinceFitting);

                if (totalKmOnTyre.compareTo(threshold85) < 0) return;

                // Check if we need to alert: every 2000 km bucket
                BigDecimal bucket2k = BigDecimal.valueOf(2000);
                BigDecimal currentBucket = totalKmOnTyre.divide(bucket2k, 0, RoundingMode.FLOOR).multiply(bucket2k);
                BigDecimal lastAlerted = tire.getLastKmAlertKm() != null ? tire.getLastKmAlertKm() : BigDecimal.ZERO;

                if (currentBucket.compareTo(lastAlerted) > 0) {
                    BigDecimal kmLeft = maxKm.subtract(totalKmOnTyre);
                    String vehicle = fitting.getVehicle().getRegistrationNumber();
                    String position = fitting.getPosition().getPositionCode();

                    notificationService.sendToRoles(tenant, TYRE_ALERT_ROLES,
                            NotificationType.TYRE_EXPIRY,
                            "Tyre Life Warning",
                            tire.getSerialNumber() + " on " + vehicle + " [" + position + "] has driven " +
                            String.format("%,.0f", totalKmOnTyre) + " km. Only " +
                            String.format("%,.0f", kmLeft.max(BigDecimal.ZERO)) + " km remaining (max " +
                            String.format("%,.0f", maxKm) + " km).");

                    tire.setLastKmAlertKm(currentBucket);
                    tireRepository.save(tire);
                }
            });
        }
    }

    // ── 3. Rotation Due Alerts ────────────────────────────────────────────────
    // Alert at 90% of rotationIntervalKm, per vehicle
    private void checkRotationDueAlerts(Tenant tenant) {
        // Group by vehicle
        fittingRepository.findAllActiveFittingsByTenantId(tenant.getId()).stream()
                .collect(java.util.stream.Collectors.groupingBy(f -> f.getVehicle().getId()))
                .forEach((vehicleId, fittings) -> {
                    if (fittings.isEmpty()) return;
                    Vehicle vehicle = fittings.get(0).getVehicle();
                    if (vehicle.getTyreRotationIntervalKm() == null) return;

                    int intervalKm = vehicle.getTyreRotationIntervalKm();
                    BigDecimal alertAt = BigDecimal.valueOf(intervalKm * 0.9);

                    // Get latest odometer
                    List<VehicleMeterReading> readings = meterReadingRepository
                            .findTopByVehicleOrderByReadingKmDesc(vehicleId);
                    if (readings.isEmpty()) return;

                    BigDecimal latestOdometer = readings.get(0).getReadingKm();

                    // Find oldest fitting (longest since rotation)
                    fittings.stream()
                            .min(java.util.Comparator.comparing(VehicleTireFitting::getFittedAtKm))
                            .ifPresent(oldestFitting -> {
                                BigDecimal kmSinceRotation = latestOdometer.subtract(oldestFitting.getFittedAtKm());
                                if (kmSinceRotation.compareTo(alertAt) >= 0) {
                                    BigDecimal kmLeft = BigDecimal.valueOf(intervalKm).subtract(kmSinceRotation);
                                    notificationService.sendToRoles(tenant,
                                            Arrays.asList(RoleName.ADMIN, RoleName.SUPERVISOR),
                                            NotificationType.TYRE_ROTATION_DUE,
                                            "Tyre Rotation Due",
                                            vehicle.getRegistrationNumber() + " is due for tyre rotation in " +
                                            String.format("%,.0f", kmLeft.max(BigDecimal.ZERO)) + " km (interval: " +
                                            String.format("%,d", intervalKm) + " km).");
                                }
                            });
                });
    }
}
