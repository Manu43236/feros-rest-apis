package com.feros.api.service.impl;

import com.feros.api.dto.request.FuelLogRequest;
import com.feros.api.dto.response.FuelLogResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.FuelPaymentMode;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.FuelLogService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FuelLogServiceImpl implements FuelLogService {

    private final VehicleFuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public FuelLogResponse create(FuelLogRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId   = SecurityUtil.getCurrentUserId();

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        User filledBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new FerosException("Order not found", HttpStatus.NOT_FOUND));
        }

        // Auto-calculate total cost if not provided
        BigDecimal totalCost = request.getTotalCost();
        if (totalCost == null && request.getLitresFilled() != null && request.getCostPerLitre() != null) {
            totalCost = request.getLitresFilled().multiply(request.getCostPerLitre())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        VehicleFuelLog log = VehicleFuelLog.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .order(order)
                .filledBy(filledBy)
                .fillDate(request.getFillDate())
                .litresFilled(request.getLitresFilled())
                .odometerReading(request.getOdometerReading())
                .costPerLitre(request.getCostPerLitre())
                .totalCost(totalCost)
                .isFullTank(request.getIsFullTank() != null ? request.getIsFullTank() : false)
                .paymentMode(request.getPaymentMode())
                .fuelStationName(request.getFuelStationName())
                .fuelStationCity(request.getFuelStationCity())
                .receiptUrl(request.getReceiptUrl())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        log = fuelLogRepository.save(log);

        // Update vehicle's current odometer and fuel level
        boolean vehicleDirty = false;
        if (request.getOdometerReading() != null) {
            vehicle.setCurrentOdometerReading(request.getOdometerReading());
            vehicleDirty = true;
        }
        if (request.getLitresFilled() != null) {
            if (Boolean.TRUE.equals(request.getIsFullTank())) {
                vehicle.setCurrentFuelLevel(vehicle.getFuelTankCapacity());
            } else {
                BigDecimal current = vehicle.getCurrentFuelLevel() != null
                        ? vehicle.getCurrentFuelLevel() : BigDecimal.ZERO;
                BigDecimal updated = current.add(request.getLitresFilled());
                if (vehicle.getFuelTankCapacity() != null
                        && updated.compareTo(vehicle.getFuelTankCapacity()) > 0) {
                    updated = vehicle.getFuelTankCapacity();
                }
                vehicle.setCurrentFuelLevel(updated);
            }
            vehicleDirty = true;
        }
        if (vehicleDirty) vehicleRepository.save(vehicle);

        return toResponse(log);
    }

    @Override
    public List<FuelLogResponse> getByVehicle(Long vehicleId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return fuelLogRepository
                .findByTenantIdAndVehicleIdAndIsActiveTrueOrderByFillDateDescIdDesc(tenantId, vehicleId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public Page<FuelLogResponse> getAll(int page, int size, Long vehicleId, String paymentMode, Boolean fullTank, String search) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "fillDate").and(Sort.by(Sort.Direction.DESC, "id")));
        FuelPaymentMode pmEnum = null;
        if (paymentMode != null && !paymentMode.isBlank()) {
            try { pmEnum = FuelPaymentMode.valueOf(paymentMode); } catch (IllegalArgumentException ignored) {}
        }
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return fuelLogRepository.findAllPaged(tenantId, vehicleId, pmEnum, fullTank, searchParam, pageable)
                .map(this::toResponse);
    }

    @Override
    public FuelLogResponse getById(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleFuelLog log = findLog(id, tenantId);
        return toResponse(log);
    }

    @Override
    @Transactional
    public FuelLogResponse update(Long id, FuelLogRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleFuelLog log = findLog(id, tenantId);

        // Capture old values before update for vehicle adjustment
        BigDecimal oldLitres = log.getLitresFilled();
        boolean    oldFull   = Boolean.TRUE.equals(log.getIsFullTank());

        if (request.getFillDate() != null)        log.setFillDate(request.getFillDate());
        if (request.getLitresFilled() != null)    log.setLitresFilled(request.getLitresFilled());
        if (request.getOdometerReading() != null) log.setOdometerReading(request.getOdometerReading());
        if (request.getCostPerLitre() != null)    log.setCostPerLitre(request.getCostPerLitre());
        if (request.getTotalCost() != null)       log.setTotalCost(request.getTotalCost());
        if (request.getIsFullTank() != null)      log.setIsFullTank(request.getIsFullTank());
        if (request.getPaymentMode() != null)     log.setPaymentMode(request.getPaymentMode());
        if (request.getFuelStationName() != null) log.setFuelStationName(request.getFuelStationName());
        if (request.getFuelStationCity() != null) log.setFuelStationCity(request.getFuelStationCity());
        if (request.getReceiptUrl() != null)      log.setReceiptUrl(request.getReceiptUrl());
        if (request.getNotes() != null)           log.setNotes(request.getNotes());

        // Update vehicle fuel level by the difference in litres
        Vehicle vehicle = log.getVehicle();
        boolean newFull = Boolean.TRUE.equals(log.getIsFullTank());
        if (newFull) {
            vehicle.setCurrentFuelLevel(vehicle.getFuelTankCapacity());
        } else if (log.getLitresFilled() != null) {
            BigDecimal prev    = oldFull ? vehicle.getFuelTankCapacity() : (oldLitres != null ? oldLitres : BigDecimal.ZERO);
            BigDecimal current = vehicle.getCurrentFuelLevel() != null ? vehicle.getCurrentFuelLevel() : BigDecimal.ZERO;
            BigDecimal updated = current.subtract(prev).add(log.getLitresFilled());
            if (updated.compareTo(BigDecimal.ZERO) < 0) updated = BigDecimal.ZERO;
            if (vehicle.getFuelTankCapacity() != null && updated.compareTo(vehicle.getFuelTankCapacity()) > 0)
                updated = vehicle.getFuelTankCapacity();
            vehicle.setCurrentFuelLevel(updated);
        }
        if (request.getOdometerReading() != null)
            vehicle.setCurrentOdometerReading(request.getOdometerReading());
        vehicleRepository.save(vehicle);

        return toResponse(fuelLogRepository.save(log));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        VehicleFuelLog log = findLog(id, tenantId);
        log.setIsActive(false);
        fuelLogRepository.save(log);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VehicleFuelLog findLog(Long id, Long tenantId) {
        VehicleFuelLog log = fuelLogRepository.findById(id)
                .orElseThrow(() -> new FerosException("Fuel log not found", HttpStatus.NOT_FOUND));
        if (!log.getTenant().getId().equals(tenantId)) {
            throw new FerosException("Fuel log not found", HttpStatus.NOT_FOUND);
        }
        if (!log.getIsActive()) {
            throw new FerosException("Fuel log not found", HttpStatus.NOT_FOUND);
        }
        return log;
    }

    private FuelLogResponse toResponse(VehicleFuelLog log) {
        BigDecimal mileage    = null;
        BigDecimal kmTravelled = null;

        // Calculate mileage only for full tank fills
        if (Boolean.TRUE.equals(log.getIsFullTank()) && log.getId() != null) {
            Optional<VehicleFuelLog> prev = fuelLogRepository
                    .findPreviousFullTankFill(log.getVehicle().getId(), log.getId());

            if (prev.isPresent() && prev.get().getOdometerReading() != null
                    && log.getOdometerReading() != null && log.getLitresFilled() != null
                    && log.getLitresFilled().compareTo(BigDecimal.ZERO) > 0) {

                kmTravelled = log.getOdometerReading()
                        .subtract(prev.get().getOdometerReading());

                if (kmTravelled.compareTo(BigDecimal.ZERO) > 0) {
                    mileage = kmTravelled
                            .divide(log.getLitresFilled(), 2, RoundingMode.HALF_UP);
                }
            }
        }

        return FuelLogResponse.builder()
                .id(log.getId())
                .tenantId(log.getTenant().getId())
                .vehicleId(log.getVehicle().getId())
                .vehicleRegistrationNumber(log.getVehicle().getRegistrationNumber())
                .orderId(log.getOrder() != null ? log.getOrder().getId() : null)
                .orderNumber(log.getOrder() != null ? log.getOrder().getOrderNumber() : null)
                .filledById(log.getFilledBy().getId())
                .filledByName(log.getFilledBy().getName())
                .fillDate(log.getFillDate())
                .litresFilled(log.getLitresFilled())
                .odometerReading(log.getOdometerReading())
                .costPerLitre(log.getCostPerLitre())
                .totalCost(log.getTotalCost())
                .isFullTank(log.getIsFullTank())
                .paymentMode(log.getPaymentMode())
                .fuelStationName(log.getFuelStationName())
                .fuelStationCity(log.getFuelStationCity())
                .receiptUrl(log.getReceiptUrl())
                .notes(log.getNotes())
                .mileageKmPerLitre(mileage)
                .kmTravelled(kmTravelled)
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}
