package com.feros.api.service;

import com.feros.api.dto.request.EquipmentMakeRequest;
import com.feros.api.dto.request.EquipmentModelRequest;
import com.feros.api.dto.request.EquipmentTypeRequest;
import com.feros.api.dto.response.EquipmentMakeResponse;
import com.feros.api.dto.response.EquipmentModelResponse;
import com.feros.api.dto.response.EquipmentTypeResponse;

import java.util.List;

public interface EquipmentMasterService {

    // Makes
    List<EquipmentMakeResponse> getAllMakes();
    EquipmentMakeResponse getMakeById(Long id);
    EquipmentMakeResponse createMake(EquipmentMakeRequest request);
    EquipmentMakeResponse updateMake(Long id, EquipmentMakeRequest request);
    void deleteMake(Long id);

    // Models
    List<EquipmentModelResponse> getAllModels(Long makeId);
    EquipmentModelResponse getModelById(Long id);
    EquipmentModelResponse createModel(EquipmentModelRequest request);
    EquipmentModelResponse updateModel(Long id, EquipmentModelRequest request);
    void deleteModel(Long id);

    // Types
    List<EquipmentTypeResponse> getAllTypes(Long modelId);
    EquipmentTypeResponse getTypeById(Long id);
    EquipmentTypeResponse createType(EquipmentTypeRequest request);
    EquipmentTypeResponse updateType(Long id, EquipmentTypeRequest request);
    void deleteType(Long id);
}
