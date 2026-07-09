package com.feros.api.service;

import com.feros.api.dto.request.AssignDivisionRequest;
import com.feros.api.dto.request.LeaseSessionStartRequest;
import com.feros.api.dto.request.LeaseVehicleAssignmentRequest;
import com.feros.api.dto.request.VehicleLeaseRequest;
import com.feros.api.dto.response.LeaseBillingResponse;
import com.feros.api.dto.response.LeaseDailyLogResponse;
import com.feros.api.dto.response.LeaseVehicleAssignmentResponse;
import com.feros.api.dto.response.LeaseVehicleSessionResponse;
import com.feros.api.dto.response.VehicleLeaseResponse;
import com.feros.api.enums.LeaseStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VehicleLeaseService {
    Page<VehicleLeaseResponse> getAll(int page, int size, LeaseStatus status, Long clientId);
    VehicleLeaseResponse getById(Long id);
    VehicleLeaseResponse create(VehicleLeaseRequest request);
    VehicleLeaseResponse update(Long id, VehicleLeaseRequest request);
    VehicleLeaseResponse updateStatus(Long id, LeaseStatus newStatus);
    VehicleLeaseResponse extend(Long id, LocalDate newEndDate);

    LeaseVehicleAssignmentResponse addVehicle(Long leaseId, LeaseVehicleAssignmentRequest request);
    LeaseVehicleAssignmentResponse closeVehicleAssignment(Long leaseId, Long assignmentId, BigDecimal odometerAtEnd);
    LeaseVehicleAssignmentResponse assignDivision(Long leaseId, Long assignmentId, AssignDivisionRequest request);
    List<LeaseVehicleAssignmentResponse> getVehicles(Long leaseId);

    LeaseBillingResponse getBilling(Long leaseId);

    // Sessions
    LeaseVehicleSessionResponse startSession(Long leaseId, Long assignmentId, LeaseSessionStartRequest request);
    LeaseVehicleSessionResponse endSession(Long leaseId, Long assignmentId, LocalDateTime endTime, BigDecimal odometerEnd, String notes);
    List<LeaseVehicleSessionResponse> getSessions(Long leaseId, Long assignmentId);

    // Daily Logs
    List<LeaseDailyLogResponse> getDailyLogs(Long leaseId);
    LeaseDailyLogResponse createDailyLog(Long leaseId, Long assignmentId, LocalDate date);
}
