package com.feros.api.service;

import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.response.EquipmentResponse;
import com.feros.api.enums.EquipmentWorkStatus;

import java.util.List;

public interface EquipmentService {
    List<EquipmentResponse> getAllEquipment();
    EquipmentResponse getEquipmentById(Long id);
    EquipmentResponse createEquipment(EquipmentRequest request);
    EquipmentResponse updateEquipment(Long id, EquipmentRequest request);
    EquipmentResponse updateWorkStatus(Long id, EquipmentWorkStatus workStatus);
}
