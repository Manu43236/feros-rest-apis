package com.feros.api.service;

import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.EquipmentResponse;
import com.feros.api.dto.response.MachineAssignmentHistoryResponse;
import com.feros.api.dto.response.MachineInvoiceItemResponse;
import com.feros.api.enums.EquipmentWorkStatus;

import java.time.LocalDate;
import java.util.List;

public interface EquipmentService {
    List<EquipmentResponse> getAllEquipment();
    EquipmentResponse getEquipmentById(Long id);
    EquipmentResponse createEquipment(EquipmentRequest request);
    EquipmentResponse updateEquipment(Long id, EquipmentRequest request);
    EquipmentResponse updateWorkStatus(Long id, EquipmentWorkStatus workStatus);

    // Machine detail page
    List<MachineAssignmentHistoryResponse> getMachineAssignmentHistory(Long equipmentId);
    List<DailyLogResponse> getMachineDailyLogs(Long equipmentId, LocalDate from, LocalDate to);
    List<MachineInvoiceItemResponse> getMachineInvoiceItems(Long equipmentId);
}
