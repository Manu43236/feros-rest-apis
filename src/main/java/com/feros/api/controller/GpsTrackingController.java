package com.feros.api.controller;

import com.feros.api.dto.response.*;
import com.feros.api.gps.GpsLiveStore;
import com.feros.api.gps.model.GpsPing;
import com.feros.api.gps.model.GpsRoutePoint;
import com.feros.api.repository.GpsPingRepository;
import com.feros.api.repository.GpsRoutePointRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gps")
@RequiredArgsConstructor
public class GpsTrackingController {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter IST_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GpsLiveStore            liveStore;
    private final GpsPingRepository       pingRepo;
    private final GpsRoutePointRepository routeRepo;
    private final VehicleRepository       vehicleRepo;

    @GetMapping("/vehicles/{vehicleId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<GpsLatestResponse>> getLatest(@PathVariable Long vehicleId) {
        GpsPing ping = liveStore.getLatest(vehicleId)
                .orElseGet(() -> pingRepo.findTopByVehicleIdOrderByRecordedAtUtcDesc(vehicleId).orElse(null));

        if (ping == null) return ResponseEntity.ok(ApiResponse.success("No GPS data yet", null));

        String reg = vehicleRepo.findById(vehicleId)
                .map(v -> v.getRegistrationNumber()).orElse("Unknown");

        return ResponseEntity.ok(ApiResponse.success("Latest position", toLatest(ping, reg)));
    }

    @GetMapping("/fleet")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<GpsFleetItemResponse>>> getFleet() {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        List<GpsPing> live = liveStore.getAllLatest().stream()
                .filter(p -> tenantId.equals(p.getTenantId()))
                .toList();

        // ponytail: fall back to DB when live store is empty (server restart wipes memory)
        List<GpsPing> pings = live.isEmpty()
                ? pingRepo.findLatestPerVehicleForTenant(tenantId)
                : live;

        List<GpsFleetItemResponse> fleet = pings.stream()
                .map(p -> {
                    String reg = vehicleRepo.findById(p.getVehicleId())
                            .map(v -> v.getRegistrationNumber()).orElse("Unknown");
                    return toFleetItem(p, reg);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Fleet positions", fleet));
    }

    @GetMapping("/vehicles/{vehicleId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<GpsPingResponse>>> getHistory(
            @PathVariable Long vehicleId,
            @RequestParam String from,
            @RequestParam String to) {

        LocalDateTime fromDt = LocalDateTime.parse(from);
        LocalDateTime toDt   = LocalDateTime.parse(to);

        List<GpsPingResponse> history = pingRepo.findHistory(vehicleId, fromDt, toDt)
                .stream().map(this::toPingResponse).toList();

        return ResponseEntity.ok(ApiResponse.success("Ping history", history));
    }

    @GetMapping("/vehicles/{vehicleId}/route")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<GpsRoutePointResponse>>> getRoute(
            @PathVariable Long vehicleId,
            @RequestParam String from,
            @RequestParam String to) {

        LocalDateTime fromDt = LocalDateTime.parse(from);
        LocalDateTime toDt   = LocalDateTime.parse(to);

        List<GpsRoutePointResponse> route = routeRepo
                .findByVehicleIdAndRecordedAtUtcBetweenOrderByRecordedAtUtcAsc(vehicleId, fromDt, toDt)
                .stream().map(this::toRouteResponse).toList();

        return ResponseEntity.ok(ApiResponse.success("Route points", route));
    }

    // ── mapping helpers ──────────────────────────────────────────────────────────

    private GpsLatestResponse toLatest(GpsPing p, String reg) {
        return GpsLatestResponse.builder()
                .vehicleId(p.getVehicleId())
                .registrationNumber(reg)
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .speedKmh(p.getSpeedKmh())
                .heading(p.getHeading())
                .ignitionOn(p.getIgnitionOn())
                .lastPingUtc(p.getRecordedAtUtc().toString())
                .lastPingIst(toIst(p.getRecordedAtUtc()))
                .isLive(isLive(p.getRecordedAtUtc()))
                .build();
    }

    private GpsFleetItemResponse toFleetItem(GpsPing p, String reg) {
        return GpsFleetItemResponse.builder()
                .vehicleId(p.getVehicleId())
                .registrationNumber(reg)
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .speedKmh(p.getSpeedKmh())
                .ignitionOn(p.getIgnitionOn())
                .lastPingIst(toIst(p.getRecordedAtUtc()))
                .isLive(isLive(p.getRecordedAtUtc()))
                .build();
    }

    private GpsPingResponse toPingResponse(GpsPing p) {
        return GpsPingResponse.builder()
                .id(p.getId())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .speedKmh(p.getSpeedKmh())
                .heading(p.getHeading())
                .ignitionOn(p.getIgnitionOn())
                .recordedAtIst(toIst(p.getRecordedAtUtc()))
                .packetType(p.getPacketType())
                .alertId(p.getAlertId())
                .build();
    }

    private GpsRoutePointResponse toRouteResponse(GpsRoutePoint rp) {
        return GpsRoutePointResponse.builder()
                .latitude(rp.getLatitude())
                .longitude(rp.getLongitude())
                .heading(rp.getHeading())
                .speedKmh(rp.getSpeedKmh())
                .recordedAtIst(toIst(rp.getRecordedAtUtc()))
                .build();
    }

    private String toIst(LocalDateTime utc) {
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(IST).format(IST_FMT);
    }

    private boolean isLive(LocalDateTime utc) {
        return utc.isAfter(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
    }
}
