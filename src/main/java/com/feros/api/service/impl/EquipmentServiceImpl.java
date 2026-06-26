package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.response.EquipmentResponse;
import com.feros.api.entity.Equipment;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.master.EquipmentType;
import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.EquipmentWorkStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentRepository;
import com.feros.api.repository.EquipmentTypeRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.service.EquipmentService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final TenantRepository tenantRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;

    private Long getTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findByTenantId(getTenantId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EquipmentResponse getEquipmentById(Long id) {
        return toResponse(findByIdAndTenant(id));
    }

    @Override
    @Transactional
    public EquipmentResponse createEquipment(EquipmentRequest request) {
        Long tenantId = getTenantId();
        Tenant tenant = getTenant(tenantId);

        // Slot limit check — all machines count (active + inactive)
        Integer equipmentLimit = tenant.getEquipmentCount();
        if (equipmentLimit == null || equipmentLimit <= 0) {
            throw new FerosException("No equipment subscription active for this tenant", HttpStatus.FORBIDDEN);
        }
        long current = equipmentRepository.countByTenantId(tenantId);
        if (current >= equipmentLimit) {
            throw new FerosException(
                    "Equipment slot limit reached (" + equipmentLimit + "). Contact FEROS support to upgrade.",
                    HttpStatus.FORBIDDEN);
        }

        // Serial number uniqueness
        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            if (equipmentRepository.existsBySerialNumberAndTenantId(request.getSerialNumber(), tenantId)) {
                throw new FerosException("Serial number already exists", HttpStatus.CONFLICT);
            }
        }

        EquipmentType equipmentType = findEquipmentType(request.getEquipmentTypeId());

        Equipment equipment = Equipment.builder()
                .tenant(tenant)
                .equipmentType(equipmentType)
                .serialNumber(request.getSerialNumber())
                .registrationNumber(request.getRegistrationNumber())
                .manufactureYear(request.getManufactureYear())
                .ownershipType(request.getOwnershipType())
                .meterType(equipmentType.getDefaultMeterType())
                .currentMeterReading(request.getCurrentMeterReading())
                .workStatus(EquipmentWorkStatus.AVAILABLE)
                .isActive(true)
                .notes(request.getNotes())
                .build();

        applyOwnershipFields(equipment, request);

        return toResponse(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest request) {
        Equipment equipment = findByIdAndTenant(id);

        // Serial number uniqueness (exclude self)
        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            if (equipmentRepository.existsBySerialNumberAndTenantIdAndIdNot(
                    request.getSerialNumber(), getTenantId(), id)) {
                throw new FerosException("Serial number already exists", HttpStatus.CONFLICT);
            }
        }

        if (request.getEquipmentTypeId() != null &&
                !request.getEquipmentTypeId().equals(equipment.getEquipmentType().getId())) {
            EquipmentType newType = findEquipmentType(request.getEquipmentTypeId());
            equipment.setEquipmentType(newType);
            equipment.setMeterType(newType.getDefaultMeterType());
        }

        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setRegistrationNumber(request.getRegistrationNumber());
        equipment.setManufactureYear(request.getManufactureYear());
        equipment.setOwnershipType(request.getOwnershipType());
        equipment.setCurrentMeterReading(request.getCurrentMeterReading());
        equipment.setNotes(request.getNotes());
        if (request.getIsActive() != null) equipment.setIsActive(request.getIsActive());
        if (request.getWorkStatus() != null) equipment.setWorkStatus(request.getWorkStatus());

        applyOwnershipFields(equipment, request);

        return toResponse(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public EquipmentResponse updateWorkStatus(Long id, EquipmentWorkStatus workStatus) {
        Equipment equipment = findByIdAndTenant(id);
        equipment.setWorkStatus(workStatus);
        return toResponse(equipmentRepository.save(equipment));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Equipment findByIdAndTenant(Long id) {
        return equipmentRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new FerosException("Equipment not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentType findEquipmentType(Long typeId) {
        return equipmentTypeRepository.findById(typeId)
                .orElseThrow(() -> new FerosException("Equipment type not found", HttpStatus.NOT_FOUND));
    }

    private void applyOwnershipFields(Equipment equipment, EquipmentRequest request) {
        if (request.getOwnershipType() == EquipmentOwnershipType.OWNED) {
            equipment.setIsFinanced(request.getIsFinanced() != null && request.getIsFinanced());
            equipment.setFinancerName(request.getFinancerName());
            equipment.setFinanceStartDate(request.getFinanceStartDate());
            equipment.setFinanceEndDate(request.getFinanceEndDate());
            // clear hire fields
            equipment.setHiredFrom(null);
            equipment.setHireStartDate(null);
            equipment.setHireEndDate(null);
            equipment.setHireRate(null);
            equipment.setHireRateUnit(null);
        } else {
            equipment.setHiredFrom(request.getHiredFrom());
            equipment.setHireStartDate(request.getHireStartDate());
            equipment.setHireEndDate(request.getHireEndDate());
            equipment.setHireRate(request.getHireRate());
            equipment.setHireRateUnit(request.getHireRateUnit());
            // clear finance fields
            equipment.setIsFinanced(false);
            equipment.setFinancerName(null);
            equipment.setFinanceStartDate(null);
            equipment.setFinanceEndDate(null);
        }
    }

    private EquipmentResponse toResponse(Equipment e) {
        var type = e.getEquipmentType();
        var model = type.getModel();
        var make = model.getMake();
        return EquipmentResponse.builder()
                .id(e.getId())
                .equipmentTypeId(type.getId())
                .equipmentTypeName(type.getName())
                .modelId(model.getId())
                .modelName(model.getName())
                .makeId(make.getId())
                .makeName(make.getName())
                .serialNumber(e.getSerialNumber())
                .registrationNumber(e.getRegistrationNumber())
                .manufactureYear(e.getManufactureYear())
                .ownershipType(e.getOwnershipType())
                .isFinanced(e.getIsFinanced())
                .financerName(e.getFinancerName())
                .financeStartDate(e.getFinanceStartDate())
                .financeEndDate(e.getFinanceEndDate())
                .hiredFrom(e.getHiredFrom())
                .hireStartDate(e.getHireStartDate())
                .hireEndDate(e.getHireEndDate())
                .hireRate(e.getHireRate())
                .hireRateUnit(e.getHireRateUnit())
                .meterType(e.getMeterType())
                .currentMeterReading(e.getCurrentMeterReading())
                .workStatus(e.getWorkStatus())
                .isActive(e.getIsActive())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
