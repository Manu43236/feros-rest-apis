package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentMakeRequest;
import com.feros.api.dto.request.EquipmentModelRequest;
import com.feros.api.dto.request.EquipmentTypeRequest;
import com.feros.api.dto.response.EquipmentMakeResponse;
import com.feros.api.dto.response.EquipmentModelResponse;
import com.feros.api.dto.response.EquipmentTypeResponse;
import com.feros.api.entity.master.EquipmentMake;
import com.feros.api.entity.master.EquipmentModel;
import com.feros.api.entity.master.EquipmentType;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentMakeRepository;
import com.feros.api.repository.EquipmentModelRepository;
import com.feros.api.repository.EquipmentTypeRepository;
import com.feros.api.service.EquipmentMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentMasterServiceImpl implements EquipmentMasterService {

    private final EquipmentMakeRepository makeRepository;
    private final EquipmentModelRepository modelRepository;
    private final EquipmentTypeRepository typeRepository;

    // ── Makes ──────────────────────────────────────────────────────────────────

    @Override
    public List<EquipmentMakeResponse> getAllMakes() {
        return makeRepository.findAllByIsActiveTrue().stream().map(this::toMakeResponse).toList();
    }

    @Override
    public EquipmentMakeResponse getMakeById(Long id) {
        return toMakeResponse(findMake(id));
    }

    @Override
    public EquipmentMakeResponse createMake(EquipmentMakeRequest request) {
        EquipmentMake make = EquipmentMake.builder().name(request.getName()).isActive(true).build();
        return toMakeResponse(makeRepository.save(make));
    }

    @Override
    public EquipmentMakeResponse updateMake(Long id, EquipmentMakeRequest request) {
        EquipmentMake make = findMake(id);
        make.setName(request.getName());
        return toMakeResponse(makeRepository.save(make));
    }

    @Override
    public void deleteMake(Long id) {
        EquipmentMake make = findMake(id);
        make.setIsActive(false);
        makeRepository.save(make);
    }

    // ── Models ─────────────────────────────────────────────────────────────────

    @Override
    public List<EquipmentModelResponse> getAllModels(Long makeId) {
        List<EquipmentModel> models = makeId != null
                ? modelRepository.findAllByMakeIdAndIsActiveTrue(makeId)
                : modelRepository.findAllByIsActiveTrue();
        return models.stream().map(this::toModelResponse).toList();
    }

    @Override
    public EquipmentModelResponse getModelById(Long id) {
        return toModelResponse(findModel(id));
    }

    @Override
    public EquipmentModelResponse createModel(EquipmentModelRequest request) {
        EquipmentMake make = findMake(request.getMakeId());
        EquipmentModel model = EquipmentModel.builder()
                .make(make)
                .name(request.getName())
                .isActive(true)
                .build();
        return toModelResponse(modelRepository.save(model));
    }

    @Override
    public EquipmentModelResponse updateModel(Long id, EquipmentModelRequest request) {
        EquipmentModel model = findModel(id);
        model.setMake(findMake(request.getMakeId()));
        model.setName(request.getName());
        return toModelResponse(modelRepository.save(model));
    }

    @Override
    public void deleteModel(Long id) {
        EquipmentModel model = findModel(id);
        model.setIsActive(false);
        modelRepository.save(model);
    }

    // ── Types ──────────────────────────────────────────────────────────────────

    @Override
    public List<EquipmentTypeResponse> getAllTypes(Long modelId) {
        List<EquipmentType> types = modelId != null
                ? typeRepository.findAllByModelIdAndIsActiveTrue(modelId)
                : typeRepository.findAllByIsActiveTrue();
        return types.stream().map(this::toTypeResponse).toList();
    }

    @Override
    public EquipmentTypeResponse getTypeById(Long id) {
        return toTypeResponse(findType(id));
    }

    @Override
    public EquipmentTypeResponse createType(EquipmentTypeRequest request) {
        EquipmentModel model = findModel(request.getModelId());
        EquipmentType type = EquipmentType.builder()
                .model(model)
                .name(request.getName())
                .defaultMeterType(request.getDefaultMeterType())
                .capacity(request.getCapacity())
                .capacityUnit(request.getCapacityUnit())
                .isActive(true)
                .build();
        return toTypeResponse(typeRepository.save(type));
    }

    @Override
    public EquipmentTypeResponse updateType(Long id, EquipmentTypeRequest request) {
        EquipmentType type = findType(id);
        type.setModel(findModel(request.getModelId()));
        type.setName(request.getName());
        type.setDefaultMeterType(request.getDefaultMeterType());
        type.setCapacity(request.getCapacity());
        type.setCapacityUnit(request.getCapacityUnit());
        return toTypeResponse(typeRepository.save(type));
    }

    @Override
    public void deleteType(Long id) {
        EquipmentType type = findType(id);
        type.setIsActive(false);
        typeRepository.save(type);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private EquipmentMake findMake(Long id) {
        return makeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Equipment make not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentModel findModel(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new FerosException("Equipment model not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentType findType(Long id) {
        return typeRepository.findById(id)
                .orElseThrow(() -> new FerosException("Equipment type not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentMakeResponse toMakeResponse(EquipmentMake m) {
        return EquipmentMakeResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .isActive(m.getIsActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private EquipmentModelResponse toModelResponse(EquipmentModel m) {
        return EquipmentModelResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .makeId(m.getMake().getId())
                .makeName(m.getMake().getName())
                .isActive(m.getIsActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private EquipmentTypeResponse toTypeResponse(EquipmentType t) {
        EquipmentModel model = t.getModel();
        return EquipmentTypeResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .modelId(model.getId())
                .modelName(model.getName())
                .makeId(model.getMake().getId())
                .makeName(model.getMake().getName())
                .defaultMeterType(t.getDefaultMeterType())
                .capacity(t.getCapacity())
                .capacityUnit(t.getCapacityUnit())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
