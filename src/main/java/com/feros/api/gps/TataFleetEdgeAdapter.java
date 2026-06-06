package com.feros.api.gps;

import com.feros.api.dto.response.GpsFleetVehicleResponse;
import com.feros.api.dto.response.GpsProviderVehicleResponse;
import com.feros.api.entity.GpsProviderConfig;
import com.feros.api.entity.VehicleGpsMapping;
import com.feros.api.enums.GpsProviderType;
import com.feros.api.enums.GpsVehicleStatus;
import com.feros.api.gps.dto.TataAuthResponse;
import com.feros.api.gps.dto.TataVehicleSnapshot;
import com.feros.api.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TATA Fleet Edge GPS provider adapter.
 *
 * API base: https://cvp.api.tatamotors
 * Auth:     POST /auth/realms/external/protocol/openid-connect/token (form-urlencoded)
 * Data:     GET  /api/vehicle-snapshots?pageNumber=0&pageSize=500
 *
 * providerVehicleId in our mappings = registrationNumber from TATA
 * (vehicleId from TATA is the chassis number — not used for mapping)
 */
@Component
@RequiredArgsConstructor
public class TataFleetEdgeAdapter implements GpsProviderAdapter {

    private static final Logger log = LoggerFactory.getLogger(TataFleetEdgeAdapter.class);

    private static final String DEFAULT_BASE_URL  = "https://cvp.api.tatamotors";
    private static final String AUTH_PATH         = "/auth/realms/external/protocol/openid-connect/token";
    private static final String SNAPSHOTS_PATH    = "/api/vehicle-snapshots";
    private static final int    PAGE_SIZE         = 500;
    /** Refresh token 1 minute before it expires to avoid mid-request expiry */
    private static final int    EXPIRY_BUFFER_SEC = 60;

    private final EncryptionUtil encryptionUtil;
    private final RestTemplate   restTemplate;

    /** In-memory token cache: configId → [accessToken, expiresAt] */
    private final Map<Long, CachedToken> tokenCache = new java.util.concurrent.ConcurrentHashMap<>();

    private record CachedToken(String token, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    @Override
    public GpsProviderType getProviderType() {
        return GpsProviderType.TATA_FLEET_EDGE;
    }

    // ─── Connection test ─────────────────────────────────────────────────────────

    @Override
    public boolean testConnection(GpsProviderConfig config) {
        try {
            String token = authenticate(config);
            return token != null && !token.isBlank();
        } catch (Exception e) {
            log.warn("TATA Fleet Edge connection test failed for config {}: {}", config.getId(), e.getMessage());
            return false;
        }
    }

    // ─── Provider vehicles (for mapping UI) ──────────────────────────────────────

    @Override
    public List<GpsProviderVehicleResponse> fetchProviderVehicles(GpsProviderConfig config) {
        try {
            String token    = authenticate(config);
            String baseUrl  = resolveBaseUrl(config);
            List<TataVehicleSnapshot> snapshots = fetchAllSnapshots(token, baseUrl);

            return snapshots.stream()
                    .filter(s -> s.getRegistrationNumber() != null && !s.getRegistrationNumber().isBlank())
                    .map(s -> GpsProviderVehicleResponse.builder()
                            // Use registrationNumber as providerVehicleId — it's the stable, queryable identifier
                            .providerVehicleId(s.getRegistrationNumber())
                            .registrationNumber(s.getRegistrationNumber())
                            .vehicleModel(s.getVehicleModel())
                            .autoMatched(false)
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch vehicles from TATA Fleet Edge for config {}: {}", config.getId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    // ─── Live locations (for fleet map) ──────────────────────────────────────────

    @Override
    public List<GpsFleetVehicleResponse> fetchLiveLocations(GpsProviderConfig config, List<VehicleGpsMapping> mappings) {
        if (mappings.isEmpty()) return Collections.emptyList();

        try {
            String token   = authenticate(config);
            String baseUrl = resolveBaseUrl(config);
            List<TataVehicleSnapshot> snapshots = fetchAllSnapshots(token, baseUrl);

            // Index snapshots by registrationNumber for O(1) lookup
            // Our providerVehicleId = registrationNumber (set when mapping was created)
            Map<String, TataVehicleSnapshot> snapshotByRegNumber = snapshots.stream()
                    .filter(s -> s.getRegistrationNumber() != null)
                    .collect(Collectors.toMap(
                            TataVehicleSnapshot::getRegistrationNumber,
                            s -> s,
                            (a, b) -> a // keep first if duplicate
                    ));

            List<GpsFleetVehicleResponse> results = new ArrayList<>();

            for (VehicleGpsMapping mapping : mappings) {
                TataVehicleSnapshot snap = snapshotByRegNumber.get(mapping.getProviderVehicleId());
                if (snap == null) {
                    // Vehicle in our mapping not found in TATA response — treat as offline
                    results.add(buildOfflineResponse(mapping));
                    continue;
                }

                LocalDateTime updatedAt  = parseEventDateTime(snap.getEventDateTime());
                GpsVehicleStatus status  = resolveStatus(snap.getSpeed(), snap.getIgnitionOn(), updatedAt);

                String driverName = null;
                if (mapping.getVehicle() != null && mapping.getVehicle().getCurrentDriver() != null) {
                    driverName = mapping.getVehicle().getCurrentDriver().getName();
                }

                results.add(GpsFleetVehicleResponse.builder()
                        .vehicleId(mapping.getVehicle().getId())
                        .registrationNumber(mapping.getVehicle().getRegistrationNumber())
                        .driverName(driverName)
                        .latitude(snap.getGpsLatitude())
                        .longitude(snap.getGpsLongitude())
                        .speedKmh(snap.getSpeed())
                        .ignitionOn(snap.getIgnitionOn())
                        .gpsStatus(status)
                        .lastUpdatedAt(updatedAt)
                        .providerType(GpsProviderType.TATA_FLEET_EDGE)
                        .providerVehicleId(mapping.getProviderVehicleId())
                        .build());
            }

            return results;

        } catch (Exception e) {
            log.error("Failed to fetch live locations from TATA Fleet Edge for config {}: {}", config.getId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    // ─── Private helpers ─────────────────────────────────────────────────────────

    /**
     * OAuth2 client credentials authentication with in-memory token caching.
     * TATA Fleet Edge uses application/x-www-form-urlencoded (NOT JSON).
     * expires_in from TATA is in MINUTES.
     */
    private String authenticate(GpsProviderConfig config) {
        CachedToken cached = tokenCache.get(config.getId());
        if (cached != null && cached.isValid()) {
            return cached.token();
        }

        String clientId     = encryptionUtil.decrypt(config.getClientIdEnc());
        String clientSecret = encryptionUtil.decrypt(config.getClientSecretEnc());
        String baseUrl      = resolveBaseUrl(config);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id",     clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type",    "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        TataAuthResponse authResponse = restTemplate.postForObject(
                baseUrl + AUTH_PATH,
                request,
                TataAuthResponse.class
        );

        if (authResponse == null || authResponse.getAccessToken() == null) {
            throw new RuntimeException("TATA Fleet Edge authentication failed — no token returned");
        }

        // expires_in is in minutes — convert to seconds, subtract buffer
        int lifetimeSec = (authResponse.getExpiresInMinutes() != null
                ? authResponse.getExpiresInMinutes() * 60
                : 300) - EXPIRY_BUFFER_SEC;

        CachedToken newToken = new CachedToken(
                authResponse.getAccessToken(),
                Instant.now().plusSeconds(Math.max(lifetimeSec, 30))
        );
        tokenCache.put(config.getId(), newToken);
        log.debug("TATA Fleet Edge: new token cached for config {}, valid for {}s", config.getId(), lifetimeSec);

        return newToken.token();
    }

    /**
     * Fetches all vehicle snapshots, handling pagination automatically.
     * TATA Fleet Edge returns paginated results — we load all pages.
     */
    @SuppressWarnings("unchecked")
    private List<TataVehicleSnapshot> fetchAllSnapshots(String token, String baseUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        List<TataVehicleSnapshot> allSnapshots = new ArrayList<>();
        int pageNumber = 0;

        while (true) {
            String url = baseUrl + SNAPSHOTS_PATH + "?pageNumber=" + pageNumber + "&pageSize=" + PAGE_SIZE;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, request,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null) break;

            // TATA response may wrap data in a "content", "data", or "vehicleSnapshots" key
            // Fall back to treating the body itself as a list if no known wrapper key found
            List<Map<String, Object>> page = extractSnapshotList(body);
            if (page.isEmpty()) break;

            for (Map<String, Object> item : page) {
                TataVehicleSnapshot snap = mapToSnapshot(item);
                allSnapshots.add(snap);
            }

            // Stop if we got fewer records than a full page — no more pages
            if (page.size() < PAGE_SIZE) break;
            pageNumber++;
        }

        return allSnapshots;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractSnapshotList(Map<String, Object> body) {
        for (String key : List.of("content", "data", "vehicleSnapshots", "vehicles")) {
            Object val = body.get(key);
            if (val instanceof List) {
                return (List<Map<String, Object>>) val;
            }
        }
        // TATA might return a top-level array wrapped as a response with a known list key
        // If none found, log and return empty
        log.debug("TATA Fleet Edge: could not find snapshot list in response keys: {}", body.keySet());
        return Collections.emptyList();
    }

    private TataVehicleSnapshot mapToSnapshot(Map<String, Object> item) {
        TataVehicleSnapshot snap = new TataVehicleSnapshot();
        snap.setVehicleId(stringVal(item, "vehicleId"));
        snap.setRegistrationNumber(stringVal(item, "registrationNumber"));
        snap.setGpsLatitude(doubleVal(item, "gpsLatitude"));
        snap.setGpsLongitude(doubleVal(item, "gpsLongitude"));
        snap.setSpeed(doubleVal(item, "speed"));
        snap.setIgnitionOn(boolVal(item, "ignitionOn"));
        snap.setEventDateTime(stringVal(item, "eventDateTime"));
        snap.setVehicleModel(stringVal(item, "vehicleModel"));
        return snap;
    }

    private GpsFleetVehicleResponse buildOfflineResponse(VehicleGpsMapping mapping) {
        return GpsFleetVehicleResponse.builder()
                .vehicleId(mapping.getVehicle().getId())
                .registrationNumber(mapping.getVehicle().getRegistrationNumber())
                .driverName(null)
                .latitude(null)
                .longitude(null)
                .speedKmh(null)
                .ignitionOn(false)
                .gpsStatus(GpsVehicleStatus.OFFLINE)
                .lastUpdatedAt(null)
                .providerType(GpsProviderType.TATA_FLEET_EDGE)
                .providerVehicleId(mapping.getProviderVehicleId())
                .build();
    }

    private String resolveBaseUrl(GpsProviderConfig config) {
        return (config.getApiBaseUrl() != null && !config.getApiBaseUrl().isBlank())
                ? config.getApiBaseUrl()
                : DEFAULT_BASE_URL;
    }

    /**
     * Parses TATA eventDateTime format: "2024-01-15T10:30:00.000+0530"
     * Falls back to now() on parse failure.
     */
    private LocalDateTime parseEventDateTime(String ts) {
        if (ts == null || ts.isBlank()) return null;
        try {
            // Try OffsetDateTime first (handles timezone offset like +0530)
            return OffsetDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
                    .toLocalDateTime();
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(ts, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                log.debug("Could not parse TATA eventDateTime '{}': {}", ts, e2.getMessage());
                return null;
            }
        }
    }

    /**
     * Determines vehicle status from speed, ignition, and last update time.
     * OFFLINE: no update in last 30 minutes
     * MOVING:  speed > 5 km/h
     * IDLE:    ignition on but speed ≤ 5 km/h
     * STOPPED: ignition off and speed ≤ 5 km/h
     */
    private GpsVehicleStatus resolveStatus(Double speed, Boolean ignitionOn, LocalDateTime lastUpdatedAt) {
        if (lastUpdatedAt == null || lastUpdatedAt.isBefore(LocalDateTime.now().minusMinutes(30))) {
            return GpsVehicleStatus.OFFLINE;
        }
        if (speed != null && speed > 5.0) return GpsVehicleStatus.MOVING;
        if (Boolean.TRUE.equals(ignitionOn))  return GpsVehicleStatus.IDLE;
        return GpsVehicleStatus.STOPPED;
    }

    // ─── Map extraction helpers ───────────────────────────────────────────────────

    private String stringVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? String.valueOf(v) : null;
    }

    private Double doubleVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
            try { return Double.parseDouble((String) v); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Boolean boolVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String)  return Boolean.parseBoolean((String) v);
        return null;
    }
}
