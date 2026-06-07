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

        // Fetch snapshots — if TATA is unreachable or returns bad data, fall back to OFFLINE
        List<TataVehicleSnapshot> snapshots;
        try {
            String token   = authenticate(config);
            String baseUrl = resolveBaseUrl(config);
            snapshots = fetchAllSnapshots(token, baseUrl);
        } catch (Exception e) {
            log.error("TATA Fleet Edge snapshot fetch failed for config {}: {} — returning mapped vehicles as OFFLINE",
                    config.getId(), e.getMessage());
            return mappings.stream().map(this::buildOfflineResponse).collect(Collectors.toList());
        }

        // Index snapshots by registrationNumber for O(1) lookup
        Map<String, TataVehicleSnapshot> snapshotByRegNumber = snapshots.stream()
                .filter(s -> s.getRegistrationNumber() != null)
                .collect(Collectors.toMap(
                        TataVehicleSnapshot::getRegistrationNumber,
                        s -> s,
                        (a, b) -> a
                ));

        List<GpsFleetVehicleResponse> results = new ArrayList<>();

        for (VehicleGpsMapping mapping : mappings) {
            TataVehicleSnapshot snap = snapshotByRegNumber.get(mapping.getProviderVehicleId());
            if (snap == null) {
                // Vehicle in our mapping not found in this TATA response — show as OFFLINE
                results.add(buildOfflineResponse(mapping));
                continue;
            }

            LocalDateTime updatedAt = parseEventDateTime(snap.getEventDateTime());
            GpsVehicleStatus status = resolveStatus(snap.getSpeed(), snap.getIgnitionOn(), updatedAt);

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
     *
     * TATA response structure (per docs):
     * {
     *   "vehicles":  [ VehicleTelemetry... ],
     *   "failures":  [ ... ],
     *   "pageable":  { "pageNumber": 0, "pageSize": 100, "totalPages": 1, "totalElements": 5 }
     * }
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

            List<Map<String, Object>> page = extractVehiclesList(body);
            if (page.isEmpty()) {
                log.debug("TATA Fleet Edge: empty vehicles list on page {}. Response keys: {}", pageNumber, body.keySet());
                break;
            }

            for (Map<String, Object> item : page) {
                allSnapshots.add(mapToSnapshot(item));
            }

            // Use totalPages from pageable to decide if there are more pages
            int totalPages = extractTotalPages(body);
            if (totalPages > 0 && pageNumber + 1 >= totalPages) break;
            if (page.size() < PAGE_SIZE) break; // fallback if pageable not present
            pageNumber++;
        }

        return allSnapshots;
    }

    /**
     * Extracts the vehicle list from TATA's AllVehicleTelemetryResponse.
     * Per docs, vehicles are under the "vehicles" key.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractVehiclesList(Map<String, Object> body) {
        Object val = body.get("vehicles");
        if (val instanceof List) return (List<Map<String, Object>>) val;
        // Fallback: some older versions may use different keys
        for (String key : List.of("data", "content", "vehicleSnapshots")) {
            Object v = body.get(key);
            if (v instanceof List) return (List<Map<String, Object>>) v;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private int extractTotalPages(Map<String, Object> body) {
        Object pageable = body.get("pageable");
        if (pageable instanceof Map) {
            Object tp = ((Map<String, Object>) pageable).get("totalPages");
            if (tp instanceof Number) return ((Number) tp).intValue();
        }
        return 0;
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
     * Parses TATA eventDateTime.
     *
     * Per docs format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     * Real example from API: "2023-09-14T12:11:38.0000009" (7 fractional digits, no timezone)
     *
     * Uses ISO_OFFSET_DATE_TIME which handles variable-length fractional seconds and
     * both +05:30 (with colon) and +0530 (without colon) timezone formats.
     * Falls back to ISO_LOCAL_DATE_TIME for timestamps without timezone.
     */
    private LocalDateTime parseEventDateTime(String ts) {
        if (ts == null || ts.isBlank()) return null;
        try {
            return OffsetDateTime.parse(ts, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        } catch (Exception e1) {
            try {
                // No timezone offset — parse directly as LocalDateTime
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
