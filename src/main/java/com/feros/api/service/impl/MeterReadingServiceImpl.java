package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.MeterReadingRequest;
import com.feros.api.dto.response.MeterReadingResponse;
import com.feros.api.entity.*;
import com.feros.api.exception.FerosException;
import com.feros.api.enums.MeterReadingType;
import com.feros.api.repository.*;
import com.feros.api.service.MeterReadingService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {

    private static final int SERVICE_ALERT_BUFFER_KM = 500;

    private final VehicleMeterReadingRepository meterReadingRepository;
    private final VehicleRepository vehicleRepository;
    private final LrRepository lrRepository;
    private final UserRepository userRepository;
    private final VehicleServiceRepository vehicleServiceRepository;
    private final VehicleFuelLogRepository fuelLogRepository;

    private Long tenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    @Override
    @Transactional
    public MeterReadingResponse create(MeterReadingRequest request) {
        Long tid = tenantId();

        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(request.getVehicleId(), tid)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        // Validate reading is not less than current odometer
        if (vehicle.getCurrentOdometerReading() != null &&
                request.getReadingKm().compareTo(vehicle.getCurrentOdometerReading()) < 0) {
            throw new FerosException(
                    "Reading (" + request.getReadingKm() + " km) cannot be less than current odometer (" +
                    vehicle.getCurrentOdometerReading() + " km)", HttpStatus.BAD_REQUEST);
        }

        User recordedBy = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        Lr lr = null;
        if (request.getLrId() != null) {
            lr = lrRepository.findById(request.getLrId())
                    .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));
        }

        VehicleMeterReading reading = VehicleMeterReading.builder()
                .tenant(vehicle.getTenant())
                .vehicle(vehicle)
                .readingKm(request.getReadingKm())
                .readingType(request.getReadingType())
                .lr(lr)
                .photoUrl(request.getPhotoUrl())
                .recordedBy(recordedBy)
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : TimeUtil.nowIst())
                .notes(request.getNotes())
                .build();

        meterReadingRepository.save(reading);

        // Update vehicle's current odometer
        vehicle.setCurrentOdometerReading(request.getReadingKm());
        vehicleRepository.save(vehicle);

        // Sync fuel log odometer if this is a FUEL_FILL reading
        if (request.getReadingType() == MeterReadingType.FUEL_FILL) {
            LocalDate today = reading.getRecordedAt().toLocalDate();
            fuelLogRepository.findLatestForVehicleOnDate(vehicle.getId(),
                    today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                    .ifPresent(fuelLog -> {
                        fuelLog.setOdometerReading(request.getReadingKm());
                        fuelLogRepository.save(fuelLog);
                    });
        }

        // Check for service alerts
        int alertThreshold = request.getReadingKm().intValue() + SERVICE_ALERT_BUFFER_KM;
        List<VehicleService> alertServices = vehicleServiceRepository
                .findServicesNearOrOverdue(vehicle.getId(), alertThreshold);

        List<MeterReadingResponse.ServiceAlertDto> alerts = alertServices.stream().map(s -> {
            String status = s.getDueAtOdometer() <= request.getReadingKm().intValue() ? "OVERDUE" : "DUE_SOON";
            return MeterReadingResponse.ServiceAlertDto.builder()
                    .serviceId(s.getId())
                    .serviceNumber(s.getServiceNumber())
                    .serviceType(s.getServiceType() != null ? s.getServiceType().name() : null)
                    .dueAtOdometer(s.getDueAtOdometer())
                    .status(status)
                    .build();
        }).toList();

        return mapToResponse(reading, alerts);
    }

    @Override
    public List<MeterReadingResponse> getAll(Long vehicleId) {
        Long tid = tenantId();
        List<VehicleMeterReading> readings;
        if (vehicleId != null) {
            readings = meterReadingRepository
                    .findByVehicleIdAndTenantIdAndIsActiveTrueOrderByReadingKmDesc(vehicleId, tid);
        } else {
            readings = meterReadingRepository
                    .findByTenantIdAndIsActiveTrueOrderByRecordedAtDesc(tid);
        }
        return readings.stream().map(r -> mapToResponse(r, new ArrayList<>())).toList();
    }

    @Override
    @Transactional
    public MeterReadingResponse update(Long id, MeterReadingRequest request) {
        Long tid = tenantId();

        VehicleMeterReading reading = meterReadingRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tid)
                .orElseThrow(() -> new FerosException("Meter reading not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = reading.getVehicle();

        // Validate new reading is not less than the previous reading for this vehicle (excluding this record)
        List<VehicleMeterReading> others = meterReadingRepository
                .findTopByVehicleOrderByReadingKmDesc(vehicle.getId())
                .stream()
                .filter(r -> !r.getId().equals(id))
                .toList();

        if (!others.isEmpty() && request.getReadingKm().compareTo(others.get(0).getReadingKm()) < 0) {
            throw new FerosException(
                    "Reading (" + request.getReadingKm() + " km) cannot be less than the previous reading (" +
                    others.get(0).getReadingKm() + " km)", HttpStatus.BAD_REQUEST);
        }

        Lr lr = null;
        if (request.getLrId() != null) {
            lr = lrRepository.findById(request.getLrId())
                    .orElseThrow(() -> new FerosException("LR not found", HttpStatus.NOT_FOUND));
        }

        reading.setReadingKm(request.getReadingKm());
        reading.setReadingType(request.getReadingType());
        reading.setLr(lr);
        reading.setPhotoUrl(request.getPhotoUrl());
        reading.setNotes(request.getNotes());
        if (request.getRecordedAt() != null) reading.setRecordedAt(request.getRecordedAt());

        meterReadingRepository.save(reading);

        // Re-sync vehicle odometer to the highest active reading
        meterReadingRepository.findTopByVehicleOrderByReadingKmDesc(vehicle.getId())
                .stream().findFirst()
                .ifPresent(top -> {
                    vehicle.setCurrentOdometerReading(top.getReadingKm());
                    vehicleRepository.save(vehicle);
                });

        return mapToResponse(reading, new ArrayList<>());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        VehicleMeterReading reading = meterReadingRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId())
                .orElseThrow(() -> new FerosException("Meter reading not found", HttpStatus.NOT_FOUND));
        reading.setIsActive(false);
        meterReadingRepository.save(reading);
    }

    private MeterReadingResponse mapToResponse(VehicleMeterReading r,
                                               List<MeterReadingResponse.ServiceAlertDto> alerts) {
        return MeterReadingResponse.builder()
                .id(r.getId())
                .tenantId(r.getTenant().getId())
                .vehicleId(r.getVehicle().getId())
                .vehicleRegistrationNumber(r.getVehicle().getRegistrationNumber())
                .readingKm(r.getReadingKm())
                .readingType(r.getReadingType())
                .lrId(r.getLr() != null ? r.getLr().getId() : null)
                .lrNumber(r.getLr() != null ? r.getLr().getLrNumber() : null)
                .photoUrl(r.getPhotoUrl())
                .recordedById(r.getRecordedBy().getId())
                .recordedByName(r.getRecordedBy().getName())
                .recordedAt(r.getRecordedAt())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .serviceAlerts(alerts)
                .build();
    }
}
