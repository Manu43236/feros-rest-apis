package com.feros.api.service;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.AssignOperatorRequest;
import com.feros.api.dto.request.DailyLogRequest;
import com.feros.api.dto.request.MachineAssignmentRequest;
import com.feros.api.dto.request.MachineConditionSurveyRequest;
import com.feros.api.dto.request.StartWorkEntryRequest;
import com.feros.api.dto.request.StopWorkEntryRequest;
import com.feros.api.dto.request.SwapMachineRequest;
import com.feros.api.dto.request.WoAmendmentRequest;
import com.feros.api.dto.request.WorkOrderRequest;
import com.feros.api.dto.response.DailyLogResponse;
import com.feros.api.dto.response.MachineAssignmentResponse;
import com.feros.api.dto.response.MachineConditionSurveyResponse;
import com.feros.api.dto.response.WorkEntryResponse;
import com.feros.api.dto.response.WorkOrderDetailResponse;
import com.feros.api.dto.response.WorkOrderResponse;
import com.feros.api.dto.response.WoAmendmentResponse;
import com.feros.api.dto.response.DieselSummaryResponse;
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

    // Attachment link on a machine line (KAN-13). attachmentId null clears it.
    MachineAssignmentResponse setAttachment(Long workOrderId, Long assignmentId, Long attachmentId);

    WorkEntryResponse startWork(Long workOrderId, Long assignmentId, StartWorkEntryRequest request);

    WorkEntryResponse stopWork(Long workOrderId, Long assignmentId, StopWorkEntryRequest request);

    List<WorkEntryResponse> getWorkEntries(Long workOrderId, Long assignmentId);

    List<WorkEntryResponse> getAllWorkEntries(Long workOrderId, LocalDate from, LocalDate to);

    int convertWorkEntriesToLogs(Long workOrderId);

    List<DailyLogResponse> getLogs(Long workOrderId, LocalDate from, LocalDate to);

    DailyLogResponse addLog(Long workOrderId, DailyLogRequest request);

    DailyLogResponse updateLog(Long workOrderId, Long logId, DailyLogRequest request);

    void deleteLog(Long workOrderId, Long logId);

    // KAN-19 Amendments
    List<WoAmendmentResponse> getAmendments(Long workOrderId);
    WoAmendmentResponse createAmendment(Long workOrderId, WoAmendmentRequest request, String createdBy);

    // KAN-20 Machine swap
    MachineAssignmentResponse swapMachine(Long workOrderId, Long assignmentId, SwapMachineRequest request);

    // KAN-21 Condition surveys
    List<MachineConditionSurveyResponse> getSurveys(Long workOrderId, Long assignmentId);
    MachineConditionSurveyResponse createSurvey(Long workOrderId, Long assignmentId, MachineConditionSurveyRequest request);

    // E6 KAN-32 Diesel reconciliation
    List<DieselSummaryResponse> getDieselSummary(Long workOrderId);
}
