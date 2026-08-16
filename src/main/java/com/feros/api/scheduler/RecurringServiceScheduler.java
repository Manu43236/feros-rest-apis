package com.feros.api.scheduler;

import com.feros.api.entity.Tenant;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleMeterReading;
import com.feros.api.entity.VehicleService;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.VehicleMeterReadingRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.repository.VehicleServiceRepository;
import com.feros.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringServiceScheduler {

    private static final int ALERT_KM_THRESHOLD = 500;

    private final TenantRepository tenantRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleMeterReadingRepository meterReadingRepository;
    private final VehicleServiceRepository vehicleServiceRepository;
    private final NotificationService notificationService;

    // Runs every day at 8:30 AM (offset from TyreAlertScheduler at 8:00 AM)
    @Scheduled(cron = "0 30 8 * * *")
    @Transactional
    public void runRecurringServiceAlerts() {
        log.info("RecurringServiceScheduler started");
        for (Tenant tenant : tenantRepository.findAll()) {
            try {
                checkDueServices(tenant);
            } catch (Exception e) {
                log.error("RecurringServiceScheduler error for tenant {}: {}", tenant.getId(), e.getMessage());
            }
        }
        log.info("RecurringServiceScheduler completed");
    }

    private void checkDueServices(Tenant tenant) {
        List<Vehicle> vehicles = vehicleRepository.findByTenantIdAndIsActiveTrue(tenant.getId());

        for (Vehicle vehicle : vehicles) {
            List<VehicleMeterReading> readings = meterReadingRepository
                    .findTopByVehicleOrderByReadingKmDesc(vehicle.getId());
            if (readings.isEmpty()) continue;

            int latestOdometer = readings.get(0).getReadingKm().intValue();
            int alertThreshold = latestOdometer + ALERT_KM_THRESHOLD;

            List<VehicleService> dueServices = vehicleServiceRepository
                    .findServicesNearOrOverdue(vehicle.getId(), alertThreshold);

            for (VehicleService vs : dueServices) {
                if (vs.getDueAtOdometer() == null) continue;

                int kmLeft = vs.getDueAtOdometer() - latestOdometer;
                String serviceDesc = vs.getServiceNumber() + " | " + vehicle.getRegistrationNumber();

                if (kmLeft <= 0) {
                    notificationService.sendToRoles(tenant,
                            List.of(RoleName.SERVICE_MANAGER, RoleName.ADMIN),
                            NotificationType.SERVICE_DUE,
                            "Service Overdue — " + vehicle.getRegistrationNumber(),
                            serviceDesc + " was due at " + vs.getDueAtOdometer()
                                    + " km. Current odometer: " + latestOdometer + " km. Schedule immediately.",
                            Map.of("type", "SERVICE_COMPLETE"));
                } else {
                    notificationService.sendToRoles(tenant,
                            List.of(RoleName.SERVICE_MANAGER, RoleName.ADMIN),
                            NotificationType.SERVICE_DUE,
                            "Service Due Soon — " + vehicle.getRegistrationNumber(),
                            serviceDesc + " due in " + kmLeft + " km (at "
                                    + vs.getDueAtOdometer() + " km). Current: " + latestOdometer + " km.",
                            Map.of("type", "SERVICE_COMPLETE"));
                }
            }
        }
    }
}
