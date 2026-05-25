package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.controller.VehicleController.UpdateStatusRequest;
import com.feros.api.dto.request.VehicleRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.VehicleResponse;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleBreakdown;
import com.feros.api.entity.VehicleTyrePosition;
import com.feros.api.entity.master.*;
import com.feros.api.enums.BreakdownStatus;
import com.feros.api.enums.TyrePositionType;
import com.feros.api.enums.VehicleStatusType;
import com.feros.api.exception.FerosException;
import com.feros.api.entity.OrderVehicleAllocation;
import com.feros.api.repository.*;
import com.feros.api.service.VehicleService;
import com.feros.api.util.SecurityUtil;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final TenantRepository tenantRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final FuelTypeRepository fuelTypeRepository;
    private final OwnershipTypeRepository ownershipTypeRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final OrderVehicleAllocationRepository allocationRepository;
    private final VehicleBreakdownRepository vehicleBreakdownRepository;
    private final UserRepository userRepository;
    private final VehicleTyrePositionRepository tyrePositionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    private Long getCurrentTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private boolean isOwnedVehicle(Long ownershipTypeId) {
        if (ownershipTypeId == null) return false;
        return ownershipTypeRepository.findById(ownershipTypeId)
                .map(o -> o.getName().toUpperCase().contains("OWN"))
                .orElse(false);
    }

    private boolean isOwnedVehicleByName(String ownershipTypeName) {
        if (ownershipTypeName == null || ownershipTypeName.isBlank()) return false;
        return ownershipTypeName.toUpperCase().contains("OWN");
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        Tenant tenant = getCurrentTenant();

        // Enforce vehicle count limit (per-vehicle billing: limit = vehicleCount paid for)
        subscriptionHistoryRepository.findActiveByTenantId(tenant.getId()).stream()
                .findFirst()
                .ifPresent(h -> {
                    Integer limit = h.getVehicleCount();
                    if (limit != null && limit > 0) {
                        long current = vehicleRepository.countByTenantIdAndIsActiveTrue(tenant.getId());
                        if (current >= limit) {
                            throw new FerosException(
                                    "Vehicle limit reached (" + limit + " vehicles). Contact FEROS support to add more vehicles to your plan.",
                                    HttpStatus.FORBIDDEN);
                        }
                    }
                });

        if (request.getRegistrationNumber() == null || request.getRegistrationNumber().isBlank())
            throw new FerosException("Registration number is required", HttpStatus.BAD_REQUEST);

        String regNum = request.getRegistrationNumber().toUpperCase();
        if (isOwnedVehicle(request.getOwnershipTypeId())) {
            if (vehicleRepository.existsByRegistrationNumber(regNum))
                throw new FerosException("This owned vehicle is already registered in another fleet",
                        HttpStatus.CONFLICT);
        } else {
            if (!vehicleRepository.existsByRegistrationNumberAndOwnershipTypeNameContainingIgnoreCase(regNum, "OWN"))
                throw new FerosException("Please select a valid ownership type for this vehicle",
                        HttpStatus.BAD_REQUEST);
            if (vehicleRepository.existsByRegistrationNumberAndTenantId(regNum, tenant.getId()))
                throw new FerosException("Vehicle with this registration number already exists in your fleet",
                        HttpStatus.CONFLICT);
        }

        if (request.getFuelTankCapacity() != null && request.getCurrentFuelLevel() != null
                && request.getCurrentFuelLevel().compareTo(request.getFuelTankCapacity()) > 0)
            throw new FerosException("Current fuel level cannot exceed tank capacity", HttpStatus.BAD_REQUEST);

        Vehicle vehicle = Vehicle.builder()
                .tenant(tenant)
                .registrationNumber(regNum)
                .model(request.getModel())
                .capacityInTons(request.getCapacityInTons())
                .grossVehicleWeight(request.getGrossVehicleWeight())
                .manufactureYear(request.getManufactureYear())
                .color(request.getColor())
                .chassisNumber(request.getChassisNumber())
                .engineNumber(request.getEngineNumber())
                .ownerName(request.getOwnerName())
                .ownerPhone(request.getOwnerPhone())
                .ownerAddress(request.getOwnerAddress())
                .ownerPan(request.getOwnerPan())
                .agreementStartDate(request.getAgreementStartDate())
                .agreementEndDate(request.getAgreementEndDate())
                .agreementAmount(request.getAgreementAmount())
                .gpsDeviceNumber(request.getGpsDeviceNumber())
                .gpsDeviceImei(request.getGpsDeviceImei())
                .gpsProvider(request.getGpsProvider())
                .currentOdometerReading(request.getCurrentOdometerReading())
                .fuelTankCapacity(request.getFuelTankCapacity())
                .currentFuelLevel(request.getCurrentFuelLevel())
                .notes(request.getNotes())
                .tyreRotationIntervalKm(request.getTyreRotationIntervalKm())
                .isActive(true)
                .build();

        if (request.getBrandId() != null)
            vehicle.setBrand(vehicleBrandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new FerosException("Brand not found", HttpStatus.NOT_FOUND)));

        if (request.getVehicleTypeId() != null)
            vehicle.setVehicleType(vehicleTypeRepository.findById(request.getVehicleTypeId())
                    .orElseThrow(() -> new FerosException("Vehicle type not found", HttpStatus.NOT_FOUND)));

        if (request.getFuelTypeId() != null)
            vehicle.setFuelType(fuelTypeRepository.findById(request.getFuelTypeId())
                    .orElseThrow(() -> new FerosException("Fuel type not found", HttpStatus.NOT_FOUND)));

        if (request.getOwnershipTypeId() != null)
            vehicle.setOwnershipType(ownershipTypeRepository.findById(request.getOwnershipTypeId())
                    .orElseThrow(() -> new FerosException("Ownership type not found", HttpStatus.NOT_FOUND)));

        if (request.getCurrentStatusId() != null)
            vehicle.setCurrentStatus(vehicleStatusRepository
                    .findByIdAndIsActiveTrue(request.getCurrentStatusId())
                    .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND)));

        Vehicle saved = vehicleRepository.save(vehicle);

        // Auto-create tyre positions based on vehicle type tyre count
        if (saved.getVehicleType() != null && saved.getVehicleType().getTyreCount() != null) {
            autoCreateTyrePositions(saved, saved.getVehicleType().getTyreCount(), tenant);
        }

        return mapToResponse(saved);
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));
        VehicleResponse resp = mapToResponse(vehicle);
        allocationRepository.findActiveAllocationForVehicleOnDate(id, TimeUtil.today())
                .ifPresentOrElse(alloc -> {
                    resp.setIsAssigned(true);
                    resp.setAssignedOrderId(alloc.getOrder().getId());
                    resp.setAssignedOrderNumber(alloc.getOrder().getOrderNumber());
                }, () -> resp.setIsAssigned(false));
        return resp;
    }

    @Override
    public List<VehicleResponse> getAllVehicles(LocalDate date) {
        Long tenantId = getCurrentTenantId();
        List<Vehicle> vehicles = vehicleRepository.findByTenantId(tenantId);

        LocalDate filterDate = date != null ? date : TimeUtil.today();
        List<OrderVehicleAllocation> allocations = allocationRepository.findActiveAllocationsOnDate(tenantId, filterDate);

        // Build a map: vehicleId → allocation
        java.util.Map<Long, OrderVehicleAllocation> allocationMap = allocations.stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getVehicle().getId(),
                        a -> a,
                        (a1, a2) -> a1  // keep first if duplicate
                ));

        return vehicles.stream().map(v -> {
            VehicleResponse resp = mapToResponse(v);
            OrderVehicleAllocation alloc = allocationMap.get(v.getId());
            if (alloc != null) {
                resp.setIsAssigned(true);
                resp.setAssignedOrderId(alloc.getOrder().getId());
                resp.setAssignedOrderNumber(alloc.getOrder().getOrderNumber());
            } else {
                resp.setIsAssigned(false);
            }
            return resp;
        }).toList();
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        // Handle isActive-only update (e.g. from toggle)
        if (request.getIsActive() != null && (request.getRegistrationNumber() == null || request.getRegistrationNumber().isBlank())) {
            vehicle.setIsActive(request.getIsActive());
            return mapToResponse(vehicleRepository.save(vehicle));
        }

        if (request.getRegistrationNumber() == null || request.getRegistrationNumber().isBlank())
            throw new FerosException("Registration number is required", HttpStatus.BAD_REQUEST);

        String newRegNum = request.getRegistrationNumber().toUpperCase();
        if (!vehicle.getRegistrationNumber().equals(newRegNum)) {
            if (isOwnedVehicle(request.getOwnershipTypeId())) {
                if (vehicleRepository.existsByRegistrationNumberAndIdNot(newRegNum, id))
                    throw new FerosException("This owned vehicle is already registered in another fleet",
                            HttpStatus.CONFLICT);
            } else {
                if (!vehicleRepository.existsByRegistrationNumberAndOwnershipTypeNameContainingIgnoreCase(newRegNum, "OWN"))
                    throw new FerosException("Please select a valid ownership type for this vehicle",
                            HttpStatus.BAD_REQUEST);
                if (vehicleRepository.existsByRegistrationNumberAndTenantIdAndIdNot(newRegNum, getCurrentTenantId(), id))
                    throw new FerosException("Vehicle with this registration number already exists in your fleet",
                            HttpStatus.CONFLICT);
            }
        }
        if (request.getFuelTankCapacity() != null && request.getCurrentFuelLevel() != null
                && request.getCurrentFuelLevel().compareTo(request.getFuelTankCapacity()) > 0)
            throw new FerosException("Current fuel level cannot exceed tank capacity", HttpStatus.BAD_REQUEST);

        vehicle.setRegistrationNumber(newRegNum);
        if (request.getIsActive() != null) vehicle.setIsActive(request.getIsActive());
        vehicle.setModel(request.getModel());
        vehicle.setCapacityInTons(request.getCapacityInTons());
        vehicle.setGrossVehicleWeight(request.getGrossVehicleWeight());
        vehicle.setManufactureYear(request.getManufactureYear());
        vehicle.setColor(request.getColor());
        vehicle.setChassisNumber(request.getChassisNumber());
        vehicle.setEngineNumber(request.getEngineNumber());
        vehicle.setOwnerName(request.getOwnerName());
        vehicle.setOwnerPhone(request.getOwnerPhone());
        vehicle.setOwnerAddress(request.getOwnerAddress());
        vehicle.setOwnerPan(request.getOwnerPan());
        vehicle.setAgreementStartDate(request.getAgreementStartDate());
        vehicle.setAgreementEndDate(request.getAgreementEndDate());
        vehicle.setAgreementAmount(request.getAgreementAmount());
        vehicle.setGpsDeviceNumber(request.getGpsDeviceNumber());
        vehicle.setGpsDeviceImei(request.getGpsDeviceImei());
        vehicle.setGpsProvider(request.getGpsProvider());
        vehicle.setCurrentOdometerReading(request.getCurrentOdometerReading());
        vehicle.setFuelTankCapacity(request.getFuelTankCapacity());
        vehicle.setCurrentFuelLevel(request.getCurrentFuelLevel());
        vehicle.setNotes(request.getNotes());
        vehicle.setTyreRotationIntervalKm(request.getTyreRotationIntervalKm());

        if (request.getBrandId() != null)
            vehicle.setBrand(vehicleBrandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new FerosException("Brand not found", HttpStatus.NOT_FOUND)));

        if (request.getVehicleTypeId() != null)
            vehicle.setVehicleType(vehicleTypeRepository.findById(request.getVehicleTypeId())
                    .orElseThrow(() -> new FerosException("Vehicle type not found", HttpStatus.NOT_FOUND)));

        if (request.getFuelTypeId() != null)
            vehicle.setFuelType(fuelTypeRepository.findById(request.getFuelTypeId())
                    .orElseThrow(() -> new FerosException("Fuel type not found", HttpStatus.NOT_FOUND)));

        if (request.getOwnershipTypeId() != null)
            vehicle.setOwnershipType(ownershipTypeRepository.findById(request.getOwnershipTypeId())
                    .orElseThrow(() -> new FerosException("Ownership type not found", HttpStatus.NOT_FOUND)));

        if (request.getCurrentStatusId() != null)
            vehicle.setCurrentStatus(vehicleStatusRepository
                    .findByIdAndIsActiveTrue(request.getCurrentStatusId())
                    .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND)));

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicleStatus(Long id, UpdateStatusRequest request) {
        Long tenantId = getCurrentTenantId();

        if (request.getCurrentStatusId() == null)
            throw new FerosException("Status ID is required", HttpStatus.BAD_REQUEST);

        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        var newStatus = vehicleStatusRepository
                .findByIdAndIsActiveTrue(request.getCurrentStatusId())
                .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND));

        VehicleStatusType currentType = vehicle.getCurrentStatus() != null
                ? vehicle.getCurrentStatus().getStatusType() : null;
        VehicleStatusType newType = newStatus.getStatusType();

        // ── Transition rules ──────────────────────────────────────────────────
        if (currentType == VehicleStatusType.BREAKDOWN && newType != VehicleStatusType.IN_REPAIR) {
            throw new FerosException(
                    "Vehicle is in BREAKDOWN — it can only move to In Repair",
                    HttpStatus.BAD_REQUEST);
        }
        if (currentType == VehicleStatusType.IN_REPAIR && newType != VehicleStatusType.AVAILABLE) {
            throw new FerosException(
                    "Vehicle is In Repair — it can only move to Available",
                    HttpStatus.BAD_REQUEST);
        }
        if (currentType == VehicleStatusType.ASSIGNED || currentType == VehicleStatusType.ON_TRIP) {
            throw new FerosException(
                    "Vehicle is " + vehicle.getCurrentStatus().getName() +
                    " — status is managed by the order flow",
                    HttpStatus.BAD_REQUEST);
        }

        // ── Breakdown transition — create record ──────────────────────────────
        if (newType == VehicleStatusType.BREAKDOWN) {
            if (request.getBreakdownType() == null)
                throw new FerosException("Breakdown type is required", HttpStatus.BAD_REQUEST);
            if (request.getBreakdownDuration() == null)
                throw new FerosException("Breakdown duration (SHORT/LONG) is required", HttpStatus.BAD_REQUEST);
            if (request.getReason() == null || request.getReason().isBlank())
                throw new FerosException("Breakdown reason is required", HttpStatus.BAD_REQUEST);

            Tenant tenant = getCurrentTenant();
            var reportedBy = userRepository.findById(SecurityUtil.getCurrentUserId())
                    .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

            VehicleBreakdown breakdown = VehicleBreakdown.builder()
                    .tenant(tenant)
                    .vehicle(vehicle)
                    .breakdownDate(request.getBreakdownDate() != null
                            ? request.getBreakdownDate() : TimeUtil.nowIst())
                    .location(request.getLocation())
                    .breakdownType(request.getBreakdownType())
                    .breakdownDuration(request.getBreakdownDuration())
                    .reason(request.getReason())
                    .notes(request.getNotes())
                    .reportedBy(reportedBy)
                    .status(BreakdownStatus.REPORTED)
                    .build();

            vehicleBreakdownRepository.save(breakdown);
        }

        // ── If moving out of breakdown → resolve the active breakdown record ──
        if (currentType == VehicleStatusType.BREAKDOWN) {
            vehicleBreakdownRepository
                    .findByVehicleIdAndIsActiveTrueOrderByCreatedAtDesc(vehicle.getId())
                    .stream()
                    .filter(b -> b.getStatus() == BreakdownStatus.REPORTED && b.getOrder() == null)
                    .findFirst()
                    .ifPresent(b -> {
                        b.setStatus(BreakdownStatus.RESOLVED);
                        b.setResolvedAt(TimeUtil.nowIst());
                        vehicleBreakdownRepository.save(b);
                    });
        }

        vehicle.setCurrentStatus(newStatus);
        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse toggleVehicleActive(Long id) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));
        if (vehicle.getIsActive() && allocationRepository
                .findActiveAllocationForVehicleOnDate(id, TimeUtil.today()).isPresent()) {
            throw new FerosException(
                "Cannot deactivate vehicle — it is currently assigned to an active order. Unassign it first.",
                HttpStatus.CONFLICT);
        }
        vehicle.setIsActive(!vehicle.getIsActive());
        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));
        if (allocationRepository.findActiveAllocationForVehicleOnDate(id, TimeUtil.today()).isPresent()) {
            throw new FerosException(
                "Cannot delete vehicle — it is currently assigned to an active order. Unassign it first.",
                HttpStatus.CONFLICT);
        }
        vehicle.setIsActive(false);
        vehicleRepository.save(vehicle);
    }

    @Override
    public BulkTenantUploadResponse bulkUpload(MultipartFile file) {
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();
        int rowNum = 1;

        Tenant tenant = getCurrentTenant();

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            csvReader.readNext(); // skip header

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowNum++;
                try {
                    if (row.length < 1 || row[0].isBlank()) {
                        errors.add("Row " + rowNum + ": Registration number is required");
                        failureCount++;
                        continue;
                    }

                    String regNum = row[0].trim().toUpperCase();

                    // ownershipType (col 4) — needed for duplicate check
                    String ownershipName = (row.length > 4) ? row[4].trim() : "";
                    boolean isOwned = isOwnedVehicleByName(ownershipName);

                    if (isOwned) {
                        if (vehicleRepository.existsByRegistrationNumber(regNum)) {
                            errors.add("Row " + rowNum + ": Owned vehicle " + regNum + " is already registered in another fleet");
                            failureCount++;
                            continue;
                        }
                    } else {
                        if (!vehicleRepository.existsByRegistrationNumberAndOwnershipTypeNameContainingIgnoreCase(regNum, "OWN")) {
                            errors.add("Row " + rowNum + ": Vehicle " + regNum + " — please select a valid ownership type");
                            failureCount++;
                            continue;
                        }
                        if (vehicleRepository.existsByRegistrationNumberAndTenantId(regNum, tenant.getId())) {
                            errors.add("Row " + rowNum + ": Vehicle " + regNum + " already exists in your fleet");
                            failureCount++;
                            continue;
                        }
                    }

                    Vehicle.VehicleBuilder builder = Vehicle.builder()
                            .tenant(tenant)
                            .registrationNumber(regNum)
                            .isActive(true);

                    // vehicleType (col 1)
                    if (row.length > 1 && !row[1].isBlank()) {
                        vehicleTypeRepository.findByNameIgnoreCase(row[1].trim())
                                .ifPresent(builder::vehicleType);
                    }

                    // brand (col 2)
                    if (row.length > 2 && !row[2].isBlank()) {
                        vehicleBrandRepository.findByNameIgnoreCase(row[2].trim())
                                .ifPresent(builder::brand);
                    }

                    // fuelType (col 3)
                    if (row.length > 3 && !row[3].isBlank()) {
                        fuelTypeRepository.findByNameIgnoreCase(row[3].trim())
                                .ifPresent(builder::fuelType);
                    }

                    // ownershipType (col 4)
                    if (row.length > 4 && !row[4].isBlank()) {
                        ownershipTypeRepository.findByNameIgnoreCase(row[4].trim())
                                .ifPresent(builder::ownershipType);
                    }

                    // capacityInTons (col 5)
                    if (row.length > 5 && !row[5].isBlank()) {
                        try { builder.capacityInTons(new BigDecimal(row[5].trim())); } catch (NumberFormatException ignored) {}
                    }

                    // manufactureYear (col 6)
                    if (row.length > 6 && !row[6].isBlank()) {
                        try { builder.manufactureYear(Integer.parseInt(row[6].trim())); } catch (NumberFormatException ignored) {}
                    }

                    // color (col 7)
                    if (row.length > 7 && !row[7].isBlank()) {
                        builder.color(row[7].trim());
                    }

                    // default status → Available
                    vehicleStatusRepository.findByStatusTypeAndIsActiveTrue(VehicleStatusType.AVAILABLE)
                            .ifPresent(builder::currentStatus);

                    vehicleRepository.save(builder.build());
                    successCount++;

                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                    failureCount++;
                }
            }

        } catch (Exception e) {
            throw new FerosException("Failed to parse CSV: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return BulkTenantUploadResponse.builder()
                .totalRows(rowNum - 1)
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();
    }

    private VehicleResponse mapToResponse(Vehicle v) {
        return VehicleResponse.builder()
                .id(v.getId())
                .tenantId(v.getTenant().getId())
                .registrationNumber(v.getRegistrationNumber())
                .brandId(v.getBrand() != null ? v.getBrand().getId() : null)
                .brandName(v.getBrand() != null ? v.getBrand().getName() : null)
                .model(v.getModel())
                .vehicleTypeId(v.getVehicleType() != null ? v.getVehicleType().getId() : null)
                .vehicleTypeName(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                .fuelTypeId(v.getFuelType() != null ? v.getFuelType().getId() : null)
                .fuelTypeName(v.getFuelType() != null ? v.getFuelType().getName() : null)
                .ownershipTypeId(v.getOwnershipType() != null ? v.getOwnershipType().getId() : null)
                .ownershipTypeName(v.getOwnershipType() != null ? v.getOwnershipType().getName() : null)
                .currentStatusId(v.getCurrentStatus() != null ? v.getCurrentStatus().getId() : null)
                .currentStatusName(v.getCurrentStatus() != null ? v.getCurrentStatus().getName() : null)
                .currentStatusType(v.getCurrentStatus() != null ? v.getCurrentStatus().getStatusType() : null)
                .capacityInTons(v.getCapacityInTons())
                .grossVehicleWeight(v.getGrossVehicleWeight())
                .manufactureYear(v.getManufactureYear())
                .color(v.getColor())
                .chassisNumber(v.getChassisNumber())
                .engineNumber(v.getEngineNumber())
                .ownerName(v.getOwnerName())
                .ownerPhone(v.getOwnerPhone())
                .ownerAddress(v.getOwnerAddress())
                .ownerPan(v.getOwnerPan())
                .agreementStartDate(v.getAgreementStartDate())
                .agreementEndDate(v.getAgreementEndDate())
                .agreementAmount(v.getAgreementAmount())
                .gpsDeviceNumber(v.getGpsDeviceNumber())
                .gpsDeviceImei(v.getGpsDeviceImei())
                .gpsProvider(v.getGpsProvider())
                .currentOdometerReading(v.getCurrentOdometerReading())
                .fuelTankCapacity(v.getFuelTankCapacity())
                .currentFuelLevel(v.getCurrentFuelLevel())
                .notes(v.getNotes())
                .tyreRotationIntervalKm(v.getTyreRotationIntervalKm())
                .isActive(v.getIsActive())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public int backfillTyrePositions() {
        Long tenantId = getCurrentTenantId();
        Tenant tenant = getCurrentTenant();

        List<Vehicle> vehicles = vehicleRepository.findByTenantIdAndIsActiveTrue(tenantId);
        int count = 0;
        for (Vehicle vehicle : vehicles) {
            boolean hasPositions = tyrePositionRepository.existsByVehicleIdAndIsActiveTrue(vehicle.getId());
            if (!hasPositions && vehicle.getVehicleType() != null && vehicle.getVehicleType().getTyreCount() != null) {
                autoCreateTyrePositions(vehicle, vehicle.getVehicleType().getTyreCount(), tenant);
                count++;
            }
        }
        return count;
    }

    // ── Auto-generate tyre positions based on vehicle type tyre count ──────────
    private void autoCreateTyrePositions(Vehicle vehicle, int tyreCount, Tenant tenant) {
        List<VehicleTyrePosition> positions = new ArrayList<>();
        int order = 0;

        // Steer axle — always FL and FR
        positions.add(buildTyrePosition(tenant, vehicle, "FL", TyrePositionType.STEER, ++order));
        positions.add(buildTyrePosition(tenant, vehicle, "FR", TyrePositionType.STEER, ++order));

        // Drive positions — remaining after steer, split evenly left and right
        int driveCount = Math.max(0, tyreCount - 2);
        int perSide = driveCount / 2;

        for (int i = 1; i <= perSide; i++)
            positions.add(buildTyrePosition(tenant, vehicle, "L" + i, TyrePositionType.DRIVE, ++order));

        for (int i = 1; i <= perSide; i++)
            positions.add(buildTyrePosition(tenant, vehicle, "R" + i, TyrePositionType.DRIVE, ++order));

        // Always one spare
        positions.add(buildTyrePosition(tenant, vehicle, "SP", TyrePositionType.SPARE, ++order));

        tyrePositionRepository.saveAll(positions);
    }

    private VehicleTyrePosition buildTyrePosition(Tenant tenant, Vehicle vehicle, String code, TyrePositionType type, int order) {
        return VehicleTyrePosition.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .positionCode(code)
                .positionType(type)
                .displayOrder(order)
                .isActive(true)
                .build();
    }
}