package com.feros.api.service;

import com.feros.api.dto.request.ServicePartApprovalRequest;
import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.EquipmentServicePartResponse;

import java.util.List;

// Parts requested against equipment service tasks. Parallel to the vehicle ServicePart flow,
// reuses the SHARED spare-parts inventory + stock ledger, touches NO vehicle code.
public interface EquipmentServicePartService {
    EquipmentServicePartResponse requestPart(Long equipmentId, Long serviceId, ServicePartRequest request);
    List<EquipmentServicePartResponse> getServiceParts(Long serviceId);
    void removePart(Long partId);
    EquipmentServicePartResponse approvePart(Long partId, ServicePartApprovalRequest request);
    List<EquipmentServicePartResponse> getPending();
}
