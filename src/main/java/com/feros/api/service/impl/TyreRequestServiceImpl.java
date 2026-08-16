package com.feros.api.service.impl;

import com.feros.api.dto.request.TyreFitRequest;
import com.feros.api.dto.request.TyreRequestApproveRequest;
import com.feros.api.dto.request.TyreRequestCreateRequest;
import com.feros.api.dto.request.TyreRequestRejectRequest;
import com.feros.api.dto.response.TyreRequestResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.TyreRequestStatus;
import com.feros.api.enums.TyreStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.TyreRequestService;
import com.feros.api.service.TyreService;
import com.feros.api.util.SecurityUtil;
import com.feros.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TyreRequestServiceImpl implements TyreRequestService {

    private final TyreRequestRepository tyreRequestRepository;
    private final TenantRepository tenantRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleTyrePositionRepository positionRepository;
    private final UserRepository userRepository;
    private final TyreRepository tyreRepository;
    private final TyreService tyreService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TyreRequestResponse createRequest(TyreRequestCreateRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .filter(v -> v.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        VehicleTyrePosition position = positionRepository.findById(request.getPositionId())
                .filter(p -> p.getTenant().getId().equals(tenantId) && p.getIsActive())
                .orElseThrow(() -> new FerosException("Position not found", HttpStatus.NOT_FOUND));

        User requestedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        TyreIssueRequest tyreRequest = TyreIssueRequest.builder()
                .tenant(tenant)
                .vehicle(vehicle)
                .position(position)
                .requestedBy(requestedBy)
                .notes(request.getNotes())
                .status(TyreRequestStatus.PENDING)
                .isActive(true)
                .build();

        TyreIssueRequest saved = tyreRequestRepository.save(tyreRequest);
        notificationService.sendToRoles(tenant, List.of(RoleName.STORE_KEEPER, RoleName.ADMIN),
                NotificationType.PART_REQUESTED,
                "New Tyre Request — " + vehicle.getRegistrationNumber(),
                vehicle.getRegistrationNumber() + " | Position: " + position.getPositionCode()
                        + " | Requested by " + requestedBy.getName());
        return toResponse(saved);
    }

    @Override
    public List<TyreRequestResponse> getAll() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tyreRequestRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<TyreRequestResponse> getPending() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return tyreRequestRepository.findByTenantIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
                tenantId, TyreRequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<TyreRequestResponse> getMyRequests() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId   = SecurityUtil.getCurrentUserId();
        return tyreRequestRepository.findByTenantIdAndRequestedByIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId, userId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public TyreRequestResponse approveRequest(Long id, TyreRequestApproveRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        TyreIssueRequest tyreRequest = tyreRequestRepository.findById(id)
                .filter(r -> r.getTenant().getId().equals(tenantId) && r.getIsActive())
                .orElseThrow(() -> new FerosException("Tyre request not found", HttpStatus.NOT_FOUND));

        if (tyreRequest.getStatus() != TyreRequestStatus.PENDING) {
            throw new FerosException("Only PENDING requests can be approved", HttpStatus.BAD_REQUEST);
        }

        User approvedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        // Auto-select the oldest IN_STOCK tyre (FIFO)
        Tyre issuedTyre = tyreRepository.findFirstByTenantIdAndStatusAndIsActiveTrueOrderByIdAsc(
                        tenantId, TyreStatus.IN_STOCK)
                .orElseThrow(() -> new FerosException("No tyres available in stock", HttpStatus.BAD_REQUEST));

        // Create the fitting via TyreService — handles all validations + notifications
        TyreFitRequest fitRequest = new TyreFitRequest();
        fitRequest.setVehicleId(tyreRequest.getVehicle().getId());
        fitRequest.setTyreId(issuedTyre.getId());
        fitRequest.setPositionId(tyreRequest.getPosition().getId());
        fitRequest.setFittedAtKm(request != null ? request.getFittedAtKm() : null);
        fitRequest.setFittedDate(TimeUtil.today());

        tyreService.fitTyre(fitRequest);

        tyreRequest.setIssuedTyre(issuedTyre);
        tyreRequest.setStatus(TyreRequestStatus.APPROVED);
        tyreRequest.setApprovedBy(approvedBy);
        tyreRequest.setApprovedAt(TimeUtil.nowIst());
        tyreRequest.setFittedAtKm(request != null ? request.getFittedAtKm() : null);

        TyreIssueRequest approved = tyreRequestRepository.save(tyreRequest);
        notificationService.sendToUser(approved.getTenant(), approved.getRequestedBy(),
                NotificationType.PART_APPROVED,
                "Tyre Request Approved — " + approved.getVehicle().getRegistrationNumber(),
                "Your tyre request for " + approved.getVehicle().getRegistrationNumber()
                        + " | Position: " + approved.getPosition().getPositionCode() + " has been approved.");
        return toResponse(approved);
    }

    @Override
    @Transactional
    public TyreRequestResponse rejectRequest(Long id, TyreRequestRejectRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();

        TyreIssueRequest tyreRequest = tyreRequestRepository.findById(id)
                .filter(r -> r.getTenant().getId().equals(tenantId) && r.getIsActive())
                .orElseThrow(() -> new FerosException("Tyre request not found", HttpStatus.NOT_FOUND));

        if (tyreRequest.getStatus() != TyreRequestStatus.PENDING) {
            throw new FerosException("Only PENDING requests can be rejected", HttpStatus.BAD_REQUEST);
        }

        User rejectedBy = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        tyreRequest.setStatus(TyreRequestStatus.REJECTED);
        tyreRequest.setRejectionReason(request.getRejectionReason());
        tyreRequest.setApprovedBy(rejectedBy);
        tyreRequest.setApprovedAt(TimeUtil.nowIst());

        TyreIssueRequest rejected = tyreRequestRepository.save(tyreRequest);
        notificationService.sendToUser(rejected.getTenant(), rejected.getRequestedBy(),
                NotificationType.PART_REJECTED,
                "Tyre Request Rejected — " + rejected.getVehicle().getRegistrationNumber(),
                "Your tyre request for " + rejected.getVehicle().getRegistrationNumber()
                        + " | Position: " + rejected.getPosition().getPositionCode() + " was rejected."
                        + (request.getRejectionReason() != null ? " Reason: " + request.getRejectionReason() : ""));
        return toResponse(rejected);
    }

    private TyreRequestResponse toResponse(TyreIssueRequest r) {
        return TyreRequestResponse.builder()
                .id(r.getId())
                .tenantId(r.getTenant().getId())
                .vehicleId(r.getVehicle().getId())
                .vehicleRegistrationNumber(r.getVehicle().getRegistrationNumber())
                .positionId(r.getPosition().getId())
                .positionCode(r.getPosition().getPositionCode())
                .issuedTyreId(r.getIssuedTyre() != null ? r.getIssuedTyre().getId() : null)
                .issuedTyreSerialNumber(r.getIssuedTyre() != null ? r.getIssuedTyre().getSerialNumber() : null)
                .issuedTyreBrand(r.getIssuedTyre() != null ? r.getIssuedTyre().getBrand() : null)
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
