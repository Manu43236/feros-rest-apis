package com.feros.api.service;

import com.feros.api.dto.request.LeaseVehicleAssignmentRequest;
import com.feros.api.dto.request.VehicleLeaseRequest;
import com.feros.api.dto.response.LeaseBillingResponse;
import com.feros.api.dto.response.LeaseVehicleAssignmentResponse;
import com.feros.api.dto.response.VehicleLeaseResponse;
import com.feros.api.enums.LeaseStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    List<LeaseVehicleAssignmentResponse> getVehicles(Long leaseId);

    LeaseBillingResponse getBilling(Long leaseId);
}
