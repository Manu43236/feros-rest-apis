package com.feros.api.gps;

import com.feros.api.gps.model.GpsPing;
import com.feros.api.gps.model.GpsRoutePoint;
import com.feros.api.repository.GpsRoutePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GpsRouteService {

    private static final double ROUTE_POINT_MIN_METERS = 10.0;
    private static final double HEADING_CHANGE_DEGREES  = 15.0;

    private final GpsLiveStore           liveStore;
    private final GpsRoutePointRepository routeRepo;

    public void maybeRecord(GpsPing ping) {
        boolean shouldRecord = liveStore.getLastRoutePoint(ping.getVehicleId())
                .map(last -> {
                    double distM = GpsUtils.haversineMeters(
                            last.getLatitude().doubleValue(),  last.getLongitude().doubleValue(),
                            ping.getLatitude().doubleValue(),  ping.getLongitude().doubleValue());
                    if (distM >= ROUTE_POINT_MIN_METERS) return true;
                    if (last.getHeading() != null && ping.getHeading() != null) {
                        double diff = Math.abs(ping.getHeading() - last.getHeading());
                        if (diff > 180) diff = 360 - diff;
                        return diff > HEADING_CHANGE_DEGREES;
                    }
                    return false;
                })
                .orElse(true); // first point for this vehicle — always record

        if (shouldRecord) {
            routeRepo.save(GpsRoutePoint.builder()
                    .vehicleId(ping.getVehicleId())
                    .tenantId(ping.getTenantId())
                    .latitude(ping.getLatitude())
                    .longitude(ping.getLongitude())
                    .heading(ping.getHeading())
                    .speedKmh(ping.getSpeedKmh())
                    .recordedAtUtc(ping.getRecordedAtUtc())
                    .build());
            liveStore.setLastRoutePoint(ping.getVehicleId(), ping);
        }
    }
}
