package com.feros.api.service.impl;

import com.feros.api.dto.request.TireFitRequest;
import com.feros.api.dto.request.TireRequestApproveRequest;
import com.feros.api.dto.request.TireRequestCreateRequest;
import com.feros.api.dto.request.TireRequestRejectRequest;
import com.feros.api.dto.response.TireRequestResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.TireRequestStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.TireRequestService;
import com.feros.api.service.TireService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TireRequestServiceImpl implements TireRequestService {

    private final TireRequestRepository tireRequestRepository;
    private final TenantRepository tenantRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleTirePositionRepository positionRepository;
    private final UserRepository userRepository;
    private final TireRepository tireRepository;
    private final TireService tireService;

    @Override
    @Transactional
    public TireRequestResponse createRequest(TireRequestCreateRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .filter(v -> v.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        VehicleTirePosition position = positionRepository.findById(request.getPositionId())
                .filter(p -> p.getTenant().getId().equals(tenantId) && p.getIsActive())
                .orElseThrow(() -> new FerosException("Position not found", HttpStatus.NOT_FOUND));

        User requestedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        TireIssueRequest tireRequest = TireIssueRequest.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .position(position)
                .requestedBy(requestedBy)
                .notes(request.getNotes())
                .status(TireRequestStatus.PENDING)
                .isActive(true)
                .build();

        return toResponse(tireRequestRepository.save(tireRequest));
    }

    @Override
    public List<TireRequestResponse> getAll() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tireRequestRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<TireRequestResponse> getPending() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tireRequestRepository.findByTenantIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
                tenantId, TireRequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<TireRequestResponse> getMyRequests() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId   = SecurityUtil.getCurrentUserId();
        return tireRequestRepository.findByTenantIdAndRequestedByIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId, userId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public TireRequestResponse approveRequest(Long id, TireRequestApproveRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        TireIssueRequest tireRequest = tireRequestRepository.findById(id)
                .filter(r -> r.getTenant().getId().equals(tenantId) && r.getIsActive())
                .orElseThrow(() -> new FerosException("Tire request not found", HttpStatus.NOT_FOUND));

        if (tireRequest.getStatus() != TireRequestStatus.PENDING) {
            throw new FerosException("Only PENDING requests can be approved", HttpStatus.BAD_REQUEST);
        }

        User approvedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        // Create the fitting via TireService — handles all validations + notifications
        TireFitRequest fitRequest = new TireFitRequest();
        fitRequest.setVehicleId(tireRequest.getVehicle().getId());
        fitRequest.setTireId(request.getTireId());
        fitRequest.setPositionId(tireRequest.getPosition().getId());
        fitRequest.setFittedAtKm(request.getFittedAtKm());
        fitRequest.setFittedDate(TimeUtil.today());

        tireService.fitTire(fitRequest);

        Tire issuedTire = tireRepository.findById(request.getTireId())
                .orElseThrow(() -> new FerosException("Tire not found", HttpStatus.NOT_FOUND));
        tireRequest.setIssuedTire(issuedTire);
        tireRequest.setStatus(TireRequestStatus.APPROVED);
        tireRequest.setApprovedBy(approvedBy);
        tireRequest.setApprovedAt(TimeUtil.nowIst());
        tireRequest.setFittedAtKm(request.getFittedAtKm());

        return toResponse(tireRequestRepository.save(tireRequest));
    }

    @Override
    @Transactional
    public TireRequestResponse rejectRequest(Long id, TireRequestRejectRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        TireIssueRequest tireRequest = tireRequestRepository.findById(id)
                .filter(r -> r.getTenant().getId().equals(tenantId) && r.getIsActive())
                .orElseThrow(() -> new FerosException("Tire request not found", HttpStatus.NOT_FOUND));

        if (tireRequest.getStatus() != TireRequestStatus.PENDING) {
            throw new FerosException("Only PENDING requests can be rejected", HttpStatus.BAD_REQUEST);
        }

        User rejectedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        tireRequest.setStatus(TireRequestStatus.REJECTED);
        tireRequest.setRejectionReason(request.getRejectionReason());
        tireRequest.setApprovedBy(rejectedBy);
        tireRequest.setApprovedAt(TimeUtil.nowIst());

        return toResponse(tireRequestRepository.save(tireRequest));
    }

    private TireRequestResponse toResponse(TireIssueRequest r) {
        return TireRequestResponse.builder()
                .id(r.getId())
                .tenantId(r.getTenant().getId())
                .vehicleId(r.getVehicle().getId())
                .vehicleRegistrationNumber(r.getVehicle().getRegistrationNumber())
                .positionId(r.getPosition().getId())
                .positionCode(r.getPosition().getPositionCode())
                .issuedTireId(r.getIssuedTire() != null ? r.getIssuedTire().getId() : null)
                .issuedTireSerialNumber(r.getIssuedTire() != null ? r.getIssuedTire().getSerialNumber() : null)
                .issuedTireBrand(r.getIssuedTire() != null ? r.getIssuedTire().getBrand() : null)
                .requestedById(r.getRequestedBy().getId())
                .requestedByName(r.getRequestedBy().getName())
                .approvedById(r.getApprovedBy() != null ? r.getApprovedBy().getId() : null)
                .approvedByName(r.getApprovedBy() != null ? r.getApprovedBy().getName() : null)
                .status(r.getStatus())
                .rejectionReason(r.getRejectionReason())
                .fittedAtKm(r.getFittedAtKm())
                .notes(r.getNotes())
                .approvedAt(r.getApprovedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
