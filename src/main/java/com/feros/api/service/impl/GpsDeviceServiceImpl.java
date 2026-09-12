package com.feros.api.service.impl;

import com.feros.api.dto.request.GpsDeviceRequest;
import com.feros.api.dto.response.GpsDeviceResponse;
import com.feros.api.entity.GpsDevice;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.master.GpsDeviceModel;
import com.feros.api.enums.GpsDeviceStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.GpsDeviceModelRepository;
import com.feros.api.repository.GpsDeviceRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.service.GpsDeviceService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GpsDeviceServiceImpl implements GpsDeviceService {

    private final GpsDeviceRepository repo;
    private final GpsDeviceModelRepository modelRepo;
    private final VehicleRepository vehicleRepo;
    private final TenantRepository tenantRepo;

    @Override
    public List<GpsDeviceResponse> getAll() {
        return repo.findAllByTenantIdOrderByCreatedAtDesc(tenantId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public GpsDeviceResponse getByVehicle(Long vehicleId) {
        return repo.findByVehicleIdAndTenantIdAndStatus(vehicleId, tenantId(), GpsDeviceStatus.ACTIVE)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public GpsDeviceResponse register(GpsDeviceRequest request) {
        Tenant tenant = getTenant();
        Vehicle vehicle = vehicleRepo.findByIdAndTenantId(request.getVehicleId(), tenant.getId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));
        GpsDeviceModel model = modelRepo.findById(request.getModelId())
                .orElseThrow(() -> new FerosException("Device model not found", HttpStatus.NOT_FOUND));

        // Deactivate any existing active device on this vehicle (device swap)
        repo.findByVehicleIdAndTenantIdAndStatus(vehicle.getId(), tenant.getId(), GpsDeviceStatus.ACTIVE)
                .ifPresent(old -> {
                    old.setStatus(GpsDeviceStatus.INACTIVE);
                    repo.save(old);
                });

        // Reuse existing row if same identifier already exists (avoids unique constraint on re-assignment)
        java.util.Optional<GpsDevice> existingOpt = repo.findByDeviceIdentifier(request.getDeviceIdentifier().trim());
        if (existingOpt.isPresent()) {
            GpsDevice existing = existingOpt.get();
            if (!existing.getTenant().getId().equals(tenant.getId()) ||
                (existing.getStatus() == GpsDeviceStatus.ACTIVE)) {
                throw new FerosException(
                    "Device identifier '" + request.getDeviceIdentifier() + "' is already in use",
                    HttpStatus.CONFLICT);
            }
            existing.setVehicle(vehicle);
            existing.setModel(model);
            if (request.getCredentials() != null) existing.setCredentials(request.getCredentials());
            existing.setNotes(request.getNotes());
            existing.setStatus(GpsDeviceStatus.ACTIVE);
            return toResponse(repo.save(existing));
        }

        GpsDevice device = GpsDevice.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .model(model)
                .deviceIdentifier(request.getDeviceIdentifier().trim())
                .credentials(request.getCredentials())
                .status(GpsDeviceStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        return toResponse(repo.save(device));
    }

    @Override
    public GpsDeviceResponse update(Long id, GpsDeviceRequest request) {
        GpsDevice device = find(id);
        GpsDeviceModel model = modelRepo.findById(request.getModelId())
                .orElseThrow(() -> new FerosException("Device model not found", HttpStatus.NOT_FOUND));

        repo.findByDeviceIdentifier(request.getDeviceIdentifier().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(__ -> { throw new FerosException(
                        "Device identifier '" + request.getDeviceIdentifier() + "' is already in use",
                        HttpStatus.CONFLICT); });

        device.setModel(model);
        device.setDeviceIdentifier(request.getDeviceIdentifier().trim());
        if (request.getCredentials() != null) device.setCredentials(request.getCredentials());
        device.setNotes(request.getNotes());
        return toResponse(repo.save(device));
    }

    @Override
    public GpsDeviceResponse deactivate(Long id) {
        GpsDevice device = find(id);
        device.setStatus(GpsDeviceStatus.INACTIVE);
        return toResponse(repo.save(device));
    }

    private GpsDevice find(Long id) {
        return repo.findById(id)
                .filter(d -> d.getTenant().getId().equals(tenantId()))
                .orElseThrow(() -> new FerosException("GPS device not found", HttpStatus.NOT_FOUND));
    }

    private Long tenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getTenant() {
        return tenantRepo.findByIdAndIsActiveTrue(tenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private GpsDeviceResponse toResponse(GpsDevice d) {
        return GpsDeviceResponse.builder()
                .id(d.getId())
                .vehicleId(d.getVehicle().getId())
                .vehicleRegistrationNumber(d.getVehicle().getRegistrationNumber())
                .modelId(d.getModel().getId())
                .companyName(d.getModel().getCompanyName())
                .modelName(d.getModel().getModelName())
                .connectionType(d.getModel().getConnectionType())
                .parserKey(d.getModel().getParserKey())
                .deviceIdentifier(d.getDeviceIdentifier())
                .status(d.getStatus())
                .notes(d.getNotes())
                .lastPingAt(d.getLastPingAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
