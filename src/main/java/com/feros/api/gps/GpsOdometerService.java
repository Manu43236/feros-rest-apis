package com.feros.api.gps;

import com.feros.api.gps.model.GpsPing;
import com.feros.api.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsOdometerService {

    private final GpsLiveStore liveStore;
    private final VehicleRepository vehicleRepo;

    @Transactional
    public void accumulate(GpsPing newPing) {
        liveStore.getLatest(newPing.getVehicleId()).ifPresent(prev -> {
            double distKm = GpsUtils.haversineKm(
                    prev.getLatitude().doubleValue(),  prev.getLongitude().doubleValue(),
                    newPing.getLatitude().doubleValue(), newPing.getLongitude().doubleValue());

            if (distKm <= 0) return;

            BigDecimal km = BigDecimal.valueOf(distKm).setScale(4, RoundingMode.HALF_UP);
            vehicleRepo.findById(newPing.getVehicleId()).ifPresent(vehicle -> {
                BigDecimal current = vehicle.getCurrentOdometerReading() != null
                        ? vehicle.getCurrentOdometerReading() : BigDecimal.ZERO;
                vehicle.setCurrentOdometerReading(current.add(km).setScale(2, RoundingMode.HALF_UP));
                vehicleRepo.save(vehicle);
                log.debug("GPS odometer updated — vehicle {} +{}km total={}km",
                        newPing.getVehicleId(), km, vehicle.getCurrentOdometerReading());
            });
        });
    }
}
