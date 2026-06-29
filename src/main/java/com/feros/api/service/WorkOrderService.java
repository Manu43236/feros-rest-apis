package com.feros.api.service;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.AssignOperatorRequest;
import com.feros.api.dto.request.DailyLogRequest;
import com.feros.api.dto.request.MachineAssignmentRequest;
import com.feros.api.dto.request.StartWorkEntryRequest;
import com.feros.api.dto.request.StopWorkEntryRequest;
import com.feros.api.dto.request.WorkOrderRequest;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.MachineAssignmentResponse;
import com.feros.api.dto.response.WorkEntryResponse;
import com.feros.api.dto.response.WorkOrderDetailResponse;
import com.feros.api.dto.response.WorkOrderResponse;
import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.WorkOrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface WorkOrderService {

    Page<WorkOrderResponse> getAll(int page, int size, WorkOrderStatus status, Long clientId);

    WorkOrderDetailResponse getById(Long id);

    WorkOrderResponse create(WorkOrderRequest request);

    WorkOrderResponse update(Long id, WorkOrderRequest request);

    WorkOrderResponse updateStatus(Long id, WorkOrderStatus newStatus);

    WorkOrderResponse extend(Long id, LocalDate newEndDate);

    MachineAssignmentResponse addMachine(Long workOrderId, MachineAssignmentRequest request);

    void closeMachineAssignment(Long workOrderId, Long assignmentId, LocalDate endDate, AssignmentEndReason reason);

    MachineAssignmentResponse assignOperator(Long workOrderId, Long assignmentId, AssignOperatorRequest request);

    MachineAssignmentResponse assignDivision(Long workOrderId, Long assignmentId, AssignDivisionRequest request);

    WorkEntryResponse startWork(Long workOrderId, Long assignmentId, StartWorkEntryRequest request);

    WorkEntryResponse stopWork(Long workOrderId, Long assignmentId, StopWorkEntryRequest request);

    List<WorkEntryResponse> getWorkEntries(Long workOrderId, Long assignmentId);

    List<WorkEntryResponse> getAllWorkEntries(Long workOrderId);

    DailyLogResponse addLog(Long workOrderId, DailyLogRequest request);

    DailyLogResponse updateLog(Long workOrderId, Long logId, DailyLogRequest request);

    void deleteLog(Long workOrderId, Long logId);
}
