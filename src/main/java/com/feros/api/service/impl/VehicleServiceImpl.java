package com.feros.api.service.impl;

import com.feros.api.dto.request.VehicleRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.VehicleResponse;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.master.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.VehicleService;
import com.feros.api.util.SecurityUtil;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
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

        if (request.getRegistrationNumber() == null || request.getRegistrationNumber().isBlank())
            throw new FerosException("Registration number is required", HttpStatus.BAD_REQUEST);

        String regNum = request.getRegistrationNumber().toUpperCase();
        if (isOwnedVehicle(request.getOwnershipTypeId())) {
            if (vehicleRepository.existsByRegistrationNumber(regNum))
                throw new FerosException("This owned vehicle is already registered in another fleet",
                        HttpStatus.CONFLICT);
        } else {
            if (!vehicleRepository.existsByRegistrationNumberAndOwnershipTypeNameContainingIgnoreCase(regNum, "OWN"))
                throw new FerosException("Vehicle must be owned by a fleet before it can be hired",
                        HttpStatus.BAD_REQUEST);
            if (vehicleRepository.existsByRegistrationNumberAndTenantId(regNum, tenant.getId()))
                throw new FerosException("Vehicle with this registration number already exists in your fleet",
                        HttpStatus.CONFLICT);
        }

        Vehicle vehicle = Vehicle.builder()
                .tenant(tenant)
                .registrationNumber(regNum)
                .capacityInTons(request.getCapacityInTons())
                .manufactureYear(request.getManufactureYear())
                .color(request.getColor())
                .chassisNumber(request.getChassisNumber())
                .engineNumber(request.getEngineNumber())
                .rcNumber(request.getRcNumber())
                .rcExpiryDate(request.getRcExpiryDate())
                .insuranceCompanyName(request.getInsuranceCompanyName())
                .insurancePolicyNumber(request.getInsurancePolicyNumber())
                .insuranceStartDate(request.getInsuranceStartDate())
                .insuranceExpiryDate(request.getInsuranceExpiryDate())
                .permitNumber(request.getPermitNumber())
                .permitType(request.getPermitType())
                .permitStartDate(request.getPermitStartDate())
                .permitExpiryDate(request.getPermitExpiryDate())
                .fitnessCertificateNumber(request.getFitnessCertificateNumber())
                .fitnessExpiryDate(request.getFitnessExpiryDate())
                .pucNumber(request.getPucNumber())
                .pollutionExpiryDate(request.getPollutionExpiryDate())
                .roadTaxPaidDate(request.getRoadTaxPaidDate())
                .roadTaxExpiryDate(request.getRoadTaxExpiryDate())
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
                .notes(request.getNotes())
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
                    .findByIdAndTenantId(request.getCurrentStatusId(), tenant.getId())
                    .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND)));

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {
        return mapToResponse(vehicleRepository
                .findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToResponse).toList();
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
                    throw new FerosException("Vehicle must be owned by a fleet before it can be hired",
                            HttpStatus.BAD_REQUEST);
                if (vehicleRepository.existsByRegistrationNumberAndTenantIdAndIdNot(newRegNum, getCurrentTenantId(), id))
                    throw new FerosException("Vehicle with this registration number already exists in your fleet",
                            HttpStatus.CONFLICT);
            }
        }
        vehicle.setRegistrationNumber(newRegNum);
        if (request.getIsActive() != null) vehicle.setIsActive(request.getIsActive());
        vehicle.setCapacityInTons(request.getCapacityInTons());
        vehicle.setManufactureYear(request.getManufactureYear());
        vehicle.setColor(request.getColor());
        vehicle.setChassisNumber(request.getChassisNumber());
        vehicle.setEngineNumber(request.getEngineNumber());
        vehicle.setRcNumber(request.getRcNumber());
        vehicle.setRcExpiryDate(request.getRcExpiryDate());
        vehicle.setInsuranceCompanyName(request.getInsuranceCompanyName());
        vehicle.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        vehicle.setInsuranceStartDate(request.getInsuranceStartDate());
        vehicle.setInsuranceExpiryDate(request.getInsuranceExpiryDate());
        vehicle.setPermitNumber(request.getPermitNumber());
        vehicle.setPermitType(request.getPermitType());
        vehicle.setPermitStartDate(request.getPermitStartDate());
        vehicle.setPermitExpiryDate(request.getPermitExpiryDate());
        vehicle.setFitnessCertificateNumber(request.getFitnessCertificateNumber());
        vehicle.setFitnessExpiryDate(request.getFitnessExpiryDate());
        vehicle.setPucNumber(request.getPucNumber());
        vehicle.setPollutionExpiryDate(request.getPollutionExpiryDate());
        vehicle.setRoadTaxPaidDate(request.getRoadTaxPaidDate());
        vehicle.setRoadTaxExpiryDate(request.getRoadTaxExpiryDate());
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
        vehicle.setNotes(request.getNotes());

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
                    .findByIdAndTenantId(request.getCurrentStatusId(), getCurrentTenantId())
                    .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND)));

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse updateVehicleStatus(Long id, Long statusId) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        if (statusId == null)
            throw new FerosException("Status ID is required", HttpStatus.BAD_REQUEST);

        vehicle.setCurrentStatus(vehicleStatusRepository
                .findByIdAndTenantId(statusId, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle status not found", HttpStatus.NOT_FOUND)));

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse toggleVehicleActive(Long id) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));
        vehicle.setIsActive(!vehicle.getIsActive());
        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));
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
                            errors.add("Row " + rowNum + ": Vehicle " + regNum + " must be owned by a fleet before it can be hired");
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
                    vehicleStatusRepository.findByNameIgnoreCaseAndTenantId("Available", tenant.getId())
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
        LocalDate today = LocalDate.now();
        return VehicleResponse.builder()
                .id(v.getId())
                .tenantId(v.getTenant().getId())
                .registrationNumber(v.getRegistrationNumber())
                .brandId(v.getBrand() != null ? v.getBrand().getId() : null)
                .brandName(v.getBrand() != null ? v.getBrand().getName() : null)
                .vehicleTypeId(v.getVehicleType() != null ? v.getVehicleType().getId() : null)
                .vehicleTypeName(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                .fuelTypeId(v.getFuelType() != null ? v.getFuelType().getId() : null)
                .fuelTypeName(v.getFuelType() != null ? v.getFuelType().getName() : null)
                .ownershipTypeId(v.getOwnershipType() != null ? v.getOwnershipType().getId() : null)
                .ownershipTypeName(v.getOwnershipType() != null ? v.getOwnershipType().getName() : null)
                .currentStatusId(v.getCurrentStatus() != null ? v.getCurrentStatus().getId() : null)
                .currentStatusName(v.getCurrentStatus() != null ? v.getCurrentStatus().getName() : null)
                .capacityInTons(v.getCapacityInTons())
                .manufactureYear(v.getManufactureYear())
                .color(v.getColor())
                .chassisNumber(v.getChassisNumber())
                .engineNumber(v.getEngineNumber())
                .rcNumber(v.getRcNumber())
                .rcExpiryDate(v.getRcExpiryDate())
                .rcExpired(v.getRcExpiryDate() != null && v.getRcExpiryDate().isBefore(today))
                .insuranceCompanyName(v.getInsuranceCompanyName())
                .insurancePolicyNumber(v.getInsurancePolicyNumber())
                .insuranceStartDate(v.getInsuranceStartDate())
                .insuranceExpiryDate(v.getInsuranceExpiryDate())
                .insuranceExpired(v.getInsuranceExpiryDate() != null && v.getInsuranceExpiryDate().isBefore(today))
                .permitNumber(v.getPermitNumber())
                .permitType(v.getPermitType())
                .permitStartDate(v.getPermitStartDate())
                .permitExpiryDate(v.getPermitExpiryDate())
                .permitExpired(v.getPermitExpiryDate() != null && v.getPermitExpiryDate().isBefore(today))
                .fitnessCertificateNumber(v.getFitnessCertificateNumber())
                .fitnessExpiryDate(v.getFitnessExpiryDate())
                .fitnessExpired(v.getFitnessExpiryDate() != null && v.getFitnessExpiryDate().isBefore(today))
                .pucNumber(v.getPucNumber())
                .pollutionExpiryDate(v.getPollutionExpiryDate())
                .pollutionExpired(v.getPollutionExpiryDate() != null && v.getPollutionExpiryDate().isBefore(today))
                .roadTaxPaidDate(v.getRoadTaxPaidDate())
                .roadTaxExpiryDate(v.getRoadTaxExpiryDate())
                .roadTaxExpired(v.getRoadTaxExpiryDate() != null && v.getRoadTaxExpiryDate().isBefore(today))
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
                .notes(v.getNotes())
                .isActive(v.getIsActive())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}