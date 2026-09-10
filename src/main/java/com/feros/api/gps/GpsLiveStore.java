package com.feros.api.gps;

import com.feros.api.gps.model.GpsPing;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GpsLiveStore {

    private final Map<Long, GpsPing>     lastPing       = new ConcurrentHashMap<>();
    // ponytail: accumulate km here, flush to DB only at 1km threshold
    private final Map<Long, BigDecimal>  pendingKm      = new ConcurrentHashMap<>();
    private final Map<Long, GpsPing>     lastRoutePoint = new ConcurrentHashMap<>();

    public void update(GpsPing ping) {
        lastPing.put(ping.getVehicleId(), ping);
    }

    public Optional<GpsPing> getLatest(Long vehicleId) {
        return Optional.ofNullable(lastPing.get(vehicleId));
    }

    public Collection<GpsPing> getAllLatest() {
        return lastPing.values();
    }

    public BigDecimal addDistance(Long vehicleId, BigDecimal km) {
        BigDecimal total = pendingKm.getOrDefault(vehicleId, BigDecimal.ZERO).add(km);
        pendingKm.put(vehicleId, total);
        return total;
    }

    public void resetPendingKm(Long vehicleId) {
        pendingKm.remove(vehicleId);
    }

    public Optional<GpsPing> getLastRoutePoint(Long vehicleId) {
        return Optional.ofNullable(lastRoutePoint.get(vehicleId));
    }

    public void setLastRoutePoint(Long vehicleId, GpsPing ping) {
        lastRoutePoint.put(vehicleId, ping);
    }
}
