package com.feros.api.service;

import com.feros.api.entity.AttendanceLocation;
import com.feros.api.repository.AttendanceLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationResolverService {

    private final AttendanceLocationRepository attendanceLocationRepository;
    private final RestTemplate restTemplate;

    /**
     * Resolves a human-readable location name for a given lat/long.
     * 1. Checks predefined locations — if within radius, returns that name.
     * 2. Falls back to OpenStreetMap Nominatim reverse geocoding.
     * 3. Returns null silently if both fail (non-critical feature).
     */
    public String resolve(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return null;

        // Step 1: check predefined locations
        List<AttendanceLocation> locations = attendanceLocationRepository.findByIsActiveTrue();
        for (AttendanceLocation loc : locations) {
            double distance = haversineMeters(latitude, longitude, loc.getLatitude(), loc.getLongitude());
            if (distance <= loc.getRadiusMeters()) {
                return loc.getName();
            }
        }

        // Step 2: fallback to OpenStreetMap Nominatim
        String geocoded = reverseGeocode(latitude, longitude);
        if (geocoded != null) return geocoded;

        // Step 3: final fallback — raw coordinates
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    @SuppressWarnings("unchecked")
    private String reverseGeocode(double lat, double lon) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lon + "&format=json";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("display_name")) {
                return (String) response.get("display_name");
            }
        } catch (Exception e) {
            log.warn("Nominatim reverse geocoding failed for lat={}, lon={}: {}", lat, lon, e.getMessage());
        }
        return null;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
