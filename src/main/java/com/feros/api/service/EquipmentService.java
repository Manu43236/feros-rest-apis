package com.feros.api.service;

import com.feros.api.dto.request.EquipmentFuelLogRequest;
import com.feros.api.dto.request.EquipmentMeterReadingRequest;
import com.feros.api.dto.request.EquipmentRequest;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.EquipmentDashboardResponse;
import com.feros.api.dto.response.EquipmentFuelLogResponse;
import com.feros.api.dto.response.EquipmentMeterReadingResponse;
import com.feros.api.dto.response.EquipmentResponse;
import com.feros.api.dto.response.MachineAssignmentHistoryResponse;
import com.feros.api.dto.response.MachineInvoiceItemResponse;
import com.feros.api.enums.EquipmentWorkStatus;

import java.time.LocalDate;
import java.util.List;

public interface EquipmentService {
    EquipmentDashboardResponse getDashboard();
    List<EquipmentResponse> getAllEquipment();
    EquipmentResponse getEquipmentById(Long id);
    EquipmentResponse createEquipment(EquipmentRequest request);
    EquipmentResponse updateEquipment(Long id, EquipmentRequest request);
    EquipmentResponse updateWorkStatus(Long id, EquipmentWorkStatus workStatus);

    // Machine detail page
    List<MachineAssignmentHistoryResponse> getMachineAssignmentHistory(Long equipmentId);
    List<DailyLogResponse> getMachineDailyLogs(Long equipmentId, LocalDate from, LocalDate to);
    List<MachineInvoiceItemResponse> getMachineInvoiceItems(Long equipmentId);

    // Fuel logs
    List<EquipmentFuelLogResponse> getFuelLogs(Long equipmentId);
    EquipmentFuelLogResponse addFuelLog(Long equipmentId, EquipmentFuelLogRequest request);
    EquipmentFuelLogResponse updateFuelLog(Long equipmentId, Long logId, EquipmentFuelLogRequest request);
    void deleteFuelLog(Long equipmentId, Long logId);

    // Meter readings
    List<EquipmentMeterReadingResponse> getMeterReadings(Long equipmentId);
    EquipmentMeterReadingResponse addMeterReading(Long equipmentId, EquipmentMeterReadingRequest request);
    EquipmentMeterReadingResponse updateMeterReading(Long equipmentId, Long readingId, EquipmentMeterReadingRequest request);
    void deleteMeterReading(Long equipmentId, Long readingId);
}
