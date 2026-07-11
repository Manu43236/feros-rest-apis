package com.feros.api.service;

import com.feros.api.dto.request.EquipmentAttachmentRequest;
import com.feros.api.dto.response.EquipmentAttachmentResponse;

import java.util.List;

public interface EquipmentAttachmentService {
    List<EquipmentAttachmentResponse> getAll();
    EquipmentAttachmentResponse getById(Long id);
    EquipmentAttachmentResponse create(EquipmentAttachmentRequest request);
    EquipmentAttachmentResponse update(Long id, EquipmentAttachmentRequest request);
    EquipmentAttachmentResponse setActive(Long id, boolean active);
    void delete(Long id);
}
