package com.feros.api.service;

import com.feros.api.dto.request.DailyLogRequest;
import com.feros.api.dto.request.MachineAssignmentRequest;
import com.feros.api.dto.request.WorkOrderRequest;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.MachineAssignmentResponse;
import com.feros.api.dto.response.WorkOrderDetailResponse;
import com.feros.api.dto.response.WorkOrderResponse;
import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.WorkOrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface WorkOrderService {

    Page<WorkOrderResponse> getAll(int page, int size, WorkOrderStatus status, Long clientId);

    WorkOrderDetailResponse getById(Long id);

    WorkOrderResponse create(WorkOrderRequest request);

    WorkOrderResponse update(Long id, WorkOrderRequest request);

    WorkOrderResponse updateStatus(Long id, WorkOrderStatus newStatus);

    WorkOrderResponse extend(Long id, LocalDate newEndDate);

    MachineAssignmentResponse addMachine(Long workOrderId, MachineAssignmentRequest request);

    void closeMachineAssignment(Long workOrderId, Long assignmentId, LocalDate endDate, AssignmentEndReason reason);

    DailyLogResponse addLog(Long workOrderId, DailyLogRequest request);

    DailyLogResponse updateLog(Long workOrderId, Long logId, DailyLogRequest request);

    void deleteLog(Long workOrderId, Long logId);
}
