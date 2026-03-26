package com.feros.api.service.impl;

import com.feros.api.dto.request.VehicleServiceRequest;
import com.feros.api.dto.response.VehicleServiceResponse;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.Vehicle;
import com.feros.api.entity.VehicleService;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.VehicleRepository;
import com.feros.api.repository.VehicleServiceRepository;
import com.feros.api.service.VehicleMaintenanceService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleMaintenanceServiceImpl implements VehicleMaintenanceService {

    private final VehicleServiceRepository vehicleServiceRepository;
    private final VehicleRepository vehicleRepository;
    private final TenantRepository tenantRepository;

    @Override
    public VehicleServiceResponse create(VehicleServiceRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(request.getVehicleId(), tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        VehicleService vs = VehicleService.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .serviceDate(request.getServiceDate())
                .serviceType(request.getServiceType())
                .description(request.getDescription())
                .cost(request.getCost())
                .odometerReading(request.getOdometerReading())
                .nextServiceDueDate(request.getNextServiceDueDate())
                .nextServiceDueOdometer(request.getNextServiceDueOdometer())
                .serviceCenterName(request.getServiceCenterName())
                .serviceCenterPhone(request.getServiceCenterPhone())
                .billUrl(request.getBillUrl())
                .isActive(true)
                .build();

        return mapToResponse(vehicleServiceRepository.save(vs));
    }

    @Override
    public List<VehicleServiceResponse> getAll() {
        return vehicleServiceRepository
                .findByTenantIdAndIsActiveTrueOrderByServiceDateDesc(SecurityUtil.getCurrentTenantId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<VehicleServiceResponse> getByVehicle(Long vehicleId) {
        return vehicleServiceRepository
                .findByTenantIdAndVehicleIdAndIsActiveTrueOrderByServiceDateDesc(
                        SecurityUtil.getCurrentTenantId(), vehicleId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public VehicleServiceResponse getById(Long id) {
        return mapToResponse(vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public VehicleServiceResponse update(Long id, VehicleServiceRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        VehicleService vs = vehicleServiceRepository.findByIdAndTenantIdAndIsActiveTrue(id, tenantId)
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findByIdAndTenantIdAndIsActiveTrue(request.getVehicleId(), tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        vs.setVehicle(vehicle);
        vs.setServiceDate(request.getServiceDate());
        vs.setServiceType(request.getServiceType());
        vs.setDescription(request.getDescription());
        vs.setCost(request.getCost());
        vs.setOdometerReading(request.getOdometerReading());
        vs.setNextServiceDueDate(request.getNextServiceDueDate());
        vs.setNextServiceDueOdometer(request.getNextServiceDueOdometer());
        vs.setServiceCenterName(request.getServiceCenterName());
        vs.setServiceCenterPhone(request.getServiceCenterPhone());
        vs.setBillUrl(request.getBillUrl());

        return mapToResponse(vehicleServiceRepository.save(vs));
    }

    @Override
    public void delete(Long id) {
        VehicleService vs = vehicleServiceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Service record not found", HttpStatus.NOT_FOUND));
        vs.setIsActive(false);
        vehicleServiceRepository.save(vs);
    }

    private VehicleServiceResponse mapToResponse(VehicleService vs) {
        return VehicleServiceResponse.builder()
                .id(vs.getId())
                .tenantId(vs.getTenant().getId())
                .vehicleId(vs.getVehicle().getId())
                .vehicleRegistrationNumber(vs.getVehicle().getRegistrationNumber())
                .serviceDate(vs.getServiceDate())
                .serviceType(vs.getServiceType())
                .description(vs.getDescription())
                .cost(vs.getCost())
                .odometerReading(vs.getOdometerReading())
                .nextServiceDueDate(vs.getNextServiceDueDate())
                .nextServiceDueOdometer(vs.getNextServiceDueOdometer())
                .serviceCenterName(vs.getServiceCenterName())
                .serviceCenterPhone(vs.getServiceCenterPhone())
                .billUrl(vs.getBillUrl())
                .isActive(vs.getIsActive())
                .createdAt(vs.getCreatedAt())
                .updatedAt(vs.getUpdatedAt())
                .build();
    }
}
