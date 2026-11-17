package com.feros.api.gps;

import com.feros.api.gps.model.GpsPing;
import com.feros.api.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsOdometerService {

    private static final double WRITE_THRESHOLD_KM  = 0.25;       // flush every 250m
    private static final double MIN_HOP_KM          = 0.015;      // ignore sub-15m GPS jitter
    private static final double MAX_GAP_HOURS        = 10.0 / 60; // >10min between pings = connection break, don't bridge
    private static final double SPEED_MARGIN         = 1.5;       // allow 50% over reported speed for GPS/speed noise
    private static final double FALLBACK_SPEED_KMH   = 80.0;      // used when device reports no speed

    private final GpsLiveStore liveStore;
    private final VehicleRepository vehicleRepo;

    @Transactional
    public void accumulate(GpsPing newPing) {
        // Fix 1: buffered "history" packets replay against a stale live anchor — they inflate the
        // odometer by hundreds of km. The live marker already ignores them; so must the odometer.
        if (Boolean.TRUE.equals(newPing.getIsHistory())) return;
        // Fix 3a: engine off → any position change is GPS drift, not travel.
        if (Boolean.FALSE.equals(newPing.getIgnitionOn())) return;

        liveStore.getLatest(newPing.getVehicleId()).ifPresent(prev -> {
            double distKm = GpsUtils.haversineKm(
                    prev.getLatitude().doubleValue(),  prev.getLongitude().doubleValue(),
                    newPing.getLatitude().doubleValue(), newPing.getLongitude().doubleValue());

            // Fix 3b: below the jitter floor there's no real movement to count.
            if (distKm < MIN_HOP_KM) return;

            // Fix 2: reject physically impossible jumps (GPS teleport, or a hop that silently
            // bridges a connection gap). A hop can't exceed speed * elapsed time.
            if (!isPlausibleHop(prev, newPing, distKm)) {
                log.warn("GPS odometer: implausible {}km hop skipped — vehicle {}",
                        BigDecimal.valueOf(distKm).setScale(2, RoundingMode.HALF_UP), newPing.getVehicleId());
                return;
            }

            BigDecimal km    = BigDecimal.valueOf(distKm).setScale(4, RoundingMode.HALF_UP);
            BigDecimal total = liveStore.addDistance(newPing.getVehicleId(), km);

            if (total.doubleValue() >= WRITE_THRESHOLD_KM) {
                vehicleRepo.findById(newPing.getVehicleId()).ifPresent(vehicle -> {
                    BigDecimal current = vehicle.getCurrentOdometerReading() != null
                            ? vehicle.getCurrentOdometerReading() : BigDecimal.ZERO;
                    vehicle.setCurrentOdometerReading(current.add(total).setScale(2, RoundingMode.HALF_UP));
                    vehicleRepo.save(vehicle);
                    log.debug("GPS odometer updated — vehicle {} +{}km total={}km",
                            newPing.getVehicleId(), total, vehicle.getCurrentOdometerReading());
                });
                liveStore.resetPendingKm(newPing.getVehicleId());
            }
        });
    }

    private boolean isPlausibleHop(GpsPing prev, GpsPing cur, double distKm) {
        if (prev.getRecordedAtUtc() == null || cur.getRecordedAtUtc() == null) return false;
        double gapHours = Duration.between(prev.getRecordedAtUtc(), cur.getRecordedAtUtc()).toMillis() / 3_600_000.0;
        if (gapHours <= 0) return false;            // duplicate / out-of-order packet
        if (gapHours > MAX_GAP_HOURS) return false; // connection break — don't count the gap as driven

        double speed = Math.max(speedKmh(prev), speedKmh(cur));
        if (speed <= 0) speed = FALLBACK_SPEED_KMH;
        double maxReachableKm = speed * gapHours * SPEED_MARGIN + 0.05;
        return distKm <= maxReachableKm;
    }

    private double speedKmh(GpsPing p) {
        return p.getSpeedKmh() != null ? p.getSpeedKmh().doubleValue() : 0;
    }
}
