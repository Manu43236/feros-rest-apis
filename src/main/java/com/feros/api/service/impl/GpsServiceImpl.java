package com.feros.api.service.impl;

import com.feros.api.dto.request.GpsProviderConfigRequest;
import com.feros.api.dto.request.VehicleGpsMappingRequest;
import com.feros.api.dto.response.*;
import com.feros.api.entity.GpsProviderConfig;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleGpsMapping;
import com.feros.api.exception.FerosException;
import com.feros.api.gps.GpsProviderAdapter;
import com.feros.api.repository.*;
import com.feros.api.service.GpsService;
import com.feros.api.util.EncryptionUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GpsServiceImpl implements GpsService {

    private final GpsProviderConfigRepository configRepository;
    private final VehicleGpsMappingRepository mappingRepository;
    private final VehicleRepository vehicleRepository;
    private final TenantRepository tenantRepository;
    private final EncryptionUtil encryptionUtil;

    // All registered GPS provider adapters — Spring injects them automatically
    private final List<GpsProviderAdapter> adapters;

    // ─── Provider config ────────────────────────────────────────────────────────

    @Override
    public List<GpsProviderConfigResponse> getAllConfigs() {
        return configRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId())
                .stream()
                .map(GpsProviderConfigResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GpsProviderConfigResponse createConfig(GpsProviderConfigRequest request) {
        Tenant tenant = getTenant();
        GpsProviderConfig config = GpsProviderConfig.builder()
                .tenant(tenant)
                .providerType(request.getProviderType())
                .displayName(request.getDisplayName())
                .clientIdEnc(encryptionUtil.encrypt(request.getClientId()))
                .clientSecretEnc(encryptionUtil.encrypt(request.getClientSecret()))
                .apiBaseUrl(request.getApiBaseUrl())
                .isActive(true)
                .syncStatus("NEVER")
                .build();
        return GpsProviderConfigResponse.from(configRepository.save(config));
    }

    @Override
    @Transactional
    public GpsProviderConfigResponse updateConfig(Long id, GpsProviderConfigRequest request) {
        GpsProviderConfig config = getConfigOrThrow(id);
        config.setProviderType(request.getProviderType());
        config.setDisplayName(request.getDisplayName());
        config.setClientIdEnc(encryptionUtil.encrypt(request.getClientId()));
        config.setClientSecretEnc(encryptionUtil.encrypt(request.getClientSecret()));
        config.setApiBaseUrl(request.getApiBaseUrl());
        return GpsProviderConfigResponse.from(configRepository.save(config));
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        GpsProviderConfig config = getConfigOrThrow(id);
        config.setIsActive(false);
        configRepository.save(config);
    }

    @Override
    @Transactional
    public boolean testConnection(Long id) {
        GpsProviderConfig config = getConfigOrThrow(id);
        GpsProviderAdapter adapter = getAdapter(config);
        boolean success = adapter.testConnection(config);

        config.setLastSyncAt(LocalDateTime.now());
        config.setSyncStatus(success ? "OK" : "ERROR");
        config.setSyncErrorMsg(success ? null : "Connection test failed");
        configRepository.save(config);

        return success;
    }

    // ─── Provider vehicles ──────────────────────────────────────────────────────

    @Override
    public List<GpsProviderVehicleResponse> getProviderVehicles(Long configId) {
        GpsProviderConfig config = getConfigOrThrow(configId);
        GpsProviderAdapter adapter = getAdapter(config);

        List<GpsProviderVehicleResponse> providerVehicles = adapter.fetchProviderVehicles(config);

        // Auto-match by registration number to FEROS vehicles
        List<Vehicle> ferosVehicles = vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId());
        Map<String, Long> ferosVehicleByRegNumber = ferosVehicles.stream()
                .collect(Collectors.toMap(
                        v -> v.getRegistrationNumber().replace("-", "").replace(" ", "").toUpperCase(),
                        Vehicle::getId,
                        (a, b) -> a
                ));

        return providerVehicles.stream().map(v -> {
            String normalizedReg = v.getRegistrationNumber() != null
                    ? v.getRegistrationNumber().replace("-", "").replace(" ", "").toUpperCase()
                    : "";
            Long ferosVehicleId = ferosVehicleByRegNumber.get(normalizedReg);
            return GpsProviderVehicleResponse.builder()
                    .providerVehicleId(v.getProviderVehicleId())
                    .registrationNumber(v.getRegistrationNumber())
                    .vehicleModel(v.getVehicleModel())
                    .ferosVehicleId(ferosVehicleId)
                    .autoMatched(ferosVehicleId != null)
                    .build();
        }).collect(Collectors.toList());
    }

    // ─── Vehicle mappings ────────────────────────────────────────────────────────

    @Override
    public List<VehicleGpsMappingResponse> getAllMappings() {
        return mappingRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId())
                .stream()
                .map(VehicleGpsMappingResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleGpsMappingResponse createMapping(VehicleGpsMappingRequest request) {
        if (mappingRepository.existsByVehicleIdAndGpsProviderConfigId(
                request.getVehicleId(), request.getGpsProviderConfigId())) {
            throw new FerosException("This vehicle is already mapped to this GPS provider", HttpStatus.CONFLICT);
        }

        Vehicle vehicle = vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(request.getVehicleId(), tenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        GpsProviderConfig config = getConfigOrThrow(request.getGpsProviderConfigId());

        VehicleGpsMapping mapping = VehicleGpsMapping.builder()
                .tenant(getTenant())
                .vehicle(vehicle)
                .gpsProviderConfig(config)
                .providerVehicleId(request.getProviderVehicleId())
                .providerRegNumber(request.getProviderRegNumber())
                .isActive(true)
                .build();

        return VehicleGpsMappingResponse.from(mappingRepository.save(mapping));
    }

    @Override
    @Transactional
    public void deleteMapping(Long id) {
        VehicleGpsMapping mapping = mappingRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenantId())
                .orElseThrow(() -> new FerosException("Mapping not found", HttpStatus.NOT_FOUND));
        mapping.setIsActive(false);
        mappingRepository.save(mapping);
    }

    // ─── Fleet map ───────────────────────────────────────────────────────────────

    @Override
    public List<GpsFleetVehicleResponse> getFleet() {
        List<GpsProviderConfig> configs = configRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId());
        List<GpsFleetVehicleResponse> fleet = new ArrayList<>();

        for (GpsProviderConfig config : configs) {
            List<VehicleGpsMapping> mappings = mappingRepository.findByGpsProviderConfigIdAndIsActiveTrue(config.getId());
            if (mappings.isEmpty()) continue;

            try {
                GpsProviderAdapter adapter = getAdapter(config);
                List<GpsFleetVehicleResponse> locations = adapter.fetchLiveLocations(config, mappings);
                fleet.addAll(locations);
            } catch (Exception e) {
                // Don't fail the whole fleet if one provider is down — return others
            }
        }

        return fleet;
    }

    // ─── Private helpers ─────────────────────────────────────────────────────────

    private Long tenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private GpsProviderConfig getConfigOrThrow(Long id) {
        return configRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenantId())
                .orElseThrow(() -> new FerosException("GPS provider config not found", HttpStatus.NOT_FOUND));
    }

    private GpsProviderAdapter getAdapter(GpsProviderConfig config) {
        return adapters.stream()
                .filter(a -> a.getProviderType() == config.getProviderType())
                .findFirst()
                .orElseThrow(() -> new FerosException(
                        "No adapter found for provider: " + config.getProviderType(), HttpStatus.NOT_IMPLEMENTED));
    }
}
