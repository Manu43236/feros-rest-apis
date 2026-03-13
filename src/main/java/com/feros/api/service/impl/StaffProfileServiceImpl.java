package com.feros.api.service.impl;

import com.feros.api.dto.request.DocumentRequest;
import com.feros.api.dto.request.StaffProfileRequest;
import com.feros.api.dto.response.DocumentResponse;
import com.feros.api.dto.response.StaffProfileResponse;
import com.feros.api.entity.*;
import com.feros.api.entity.master.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.StaffProfileService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffProfileServiceImpl implements StaffProfileService {

    private final StaffProfileRepository staffProfileRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DesignationRepository designationRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final DocumentTypeRepository documentTypeRepository;

    private Long getCurrentTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public StaffProfileResponse createOrUpdateProfile(Long userId, StaffProfileRequest request) {
        Long tenantId = getCurrentTenantId();
        Tenant tenant = getCurrentTenant();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        EmploymentType employmentType = employmentTypeRepository
                .findById(request.getEmploymentTypeId())
                .orElseThrow(() -> new FerosException("Employment type not found",
                        HttpStatus.NOT_FOUND));

        StaffProfile profile = staffProfileRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElse(StaffProfile.builder()
                        .user(user)
                        .tenant(tenant)
                        .isActive(true)
                        .build());

        profile.setEmploymentType(employmentType);
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setJoiningDate(request.getJoiningDate());
        profile.setAddress(request.getAddress());
        profile.setPincode(request.getPincode());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        profile.setBankName(request.getBankName());
        profile.setAccountNumber(request.getAccountNumber());
        profile.setIfscCode(request.getIfscCode());
        profile.setAccountHolderName(request.getAccountHolderName());
        profile.setLicenseNumber(request.getLicenseNumber());
        profile.setLicenseExpiryDate(request.getLicenseExpiryDate());
        profile.setProfilePhotoUrl(request.getProfilePhotoUrl());

        if (request.getDesignationId() != null)
            profile.setDesignation(designationRepository
                    .findByIdAndTenantId(request.getDesignationId(), tenantId)
                    .orElseThrow(() -> new FerosException("Designation not found",
                            HttpStatus.NOT_FOUND)));

        if (request.getCityId() != null)
            profile.setCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new FerosException("City not found", HttpStatus.NOT_FOUND)));

        if (request.getStateId() != null)
            profile.setState(stateRepository.findById(request.getStateId())
                    .orElseThrow(() -> new FerosException("State not found",
                            HttpStatus.NOT_FOUND)));

        return mapToProfileResponse(staffProfileRepository.save(profile));
    }

    @Override
    public StaffProfileResponse getProfileByUserId(Long userId) {
        StaffProfile profile = staffProfileRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(userId, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Staff profile not found",
                        HttpStatus.NOT_FOUND));
        return mapToProfileResponse(profile);
    }

    @Override
    public List<StaffProfileResponse> getAllProfiles() {
        return staffProfileRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToProfileResponse).toList();
    }

    @Override
    @Transactional
    public DocumentResponse addStaffDocument(Long userId, DocumentRequest request) {
        Long tenantId = getCurrentTenantId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        DocumentType documentType = documentTypeRepository
                .findById(request.getDocumentTypeId())
                .orElseThrow(() -> new FerosException("Document type not found",
                        HttpStatus.NOT_FOUND));

        StaffDocument doc = StaffDocument.builder()
                .tenant(getCurrentTenant())
                .user(user)
                .documentType(documentType)
                .documentNumber(request.getDocumentNumber())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .fileUrl(request.getFileUrl())
                .isVerified(false)
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        return mapToDocumentResponse(staffDocumentRepository.save(doc));
    }

    @Override
    public List<DocumentResponse> getStaffDocuments(Long userId) {
        return staffDocumentRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(userId, getCurrentTenantId())
                .stream().map(this::mapToDocumentResponse).toList();
    }

    @Override
    @Transactional
    public DocumentResponse verifyStaffDocument(Long documentId) {
        StaffDocument doc = staffDocumentRepository
                .findByIdAndTenantIdAndIsActiveTrue(documentId, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Document not found", HttpStatus.NOT_FOUND));
        doc.setIsVerified(true);
        return mapToDocumentResponse(staffDocumentRepository.save(doc));
    }

    @Override
    @Transactional
    public DocumentResponse addVehicleDocument(Long vehicleId, DocumentRequest request) {
        Long tenantId = getCurrentTenantId();

        Vehicle vehicle = vehicleRepository
                .findByIdAndTenantIdAndIsActiveTrue(vehicleId, tenantId)
                .orElseThrow(() -> new FerosException("Vehicle not found", HttpStatus.NOT_FOUND));

        DocumentType documentType = documentTypeRepository
                .findById(request.getDocumentTypeId())
                .orElseThrow(() -> new FerosException("Document type not found",
                        HttpStatus.NOT_FOUND));

        VehicleDocument doc = VehicleDocument.builder()
                .tenant(getCurrentTenant())
                .vehicle(vehicle)
                .documentType(documentType)
                .documentNumber(request.getDocumentNumber())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .fileUrl(request.getFileUrl())
                .isVerified(false)
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        return mapToDocumentResponse(vehicleDocumentRepository.save(doc));
    }

    @Override
    public List<DocumentResponse> getVehicleDocuments(Long vehicleId) {
        return vehicleDocumentRepository
                .findByVehicleIdAndTenantIdAndIsActiveTrue(vehicleId, getCurrentTenantId())
                .stream().map(this::mapToDocumentResponse).toList();
    }

    @Override
    @Transactional
    public DocumentResponse verifyVehicleDocument(Long documentId) {
        VehicleDocument doc = vehicleDocumentRepository
                .findByIdAndTenantIdAndIsActiveTrue(documentId, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Document not found", HttpStatus.NOT_FOUND));
        doc.setIsVerified(true);
        return mapToDocumentResponse(vehicleDocumentRepository.save(doc));
    }

    // ===================== MAPPERS =====================
    private StaffProfileResponse mapToProfileResponse(StaffProfile p) {
        String roleName = p.getUser().getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse(null);

        List<DocumentResponse> documents = staffDocumentRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(p.getUser().getId(), p.getTenant().getId())
                .stream().map(this::mapToDocumentResponse).toList();

        return StaffProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .userName(p.getUser().getName())
                .userPhone(p.getUser().getPhone())
                .roleName(roleName)
                .tenantId(p.getTenant().getId())
                .designationId(p.getDesignation() != null ? p.getDesignation().getId() : null)
                .designationName(p.getDesignation() != null ? p.getDesignation().getName() : null)
                .employmentTypeId(p.getEmploymentType().getId())
                .employmentTypeName(p.getEmploymentType().getName())
                .dateOfBirth(p.getDateOfBirth())
                .joiningDate(p.getJoiningDate())
                .address(p.getAddress())
                .cityId(p.getCity() != null ? p.getCity().getId() : null)
                .cityName(p.getCity() != null ? p.getCity().getName() : null)
                .stateId(p.getState() != null ? p.getState().getId() : null)
                .stateName(p.getState() != null ? p.getState().getName() : null)
                .pincode(p.getPincode())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .bankName(p.getBankName())
                .accountNumber(p.getAccountNumber())
                .ifscCode(p.getIfscCode())
                .accountHolderName(p.getAccountHolderName())
                .licenseNumber(p.getLicenseNumber())
                .licenseExpiryDate(p.getLicenseExpiryDate())
                .licenseExpired(p.getLicenseExpiryDate() != null &&
                        p.getLicenseExpiryDate().isBefore(LocalDate.now()))
                .profilePhotoUrl(p.getProfilePhotoUrl())
                .documents(documents)
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private DocumentResponse mapToDocumentResponse(StaffDocument d) {
        return DocumentResponse.builder()
                .id(d.getId())
                .documentTypeId(d.getDocumentType().getId())
                .documentTypeName(d.getDocumentType().getName())
                .documentNumber(d.getDocumentNumber())
                .issueDate(d.getIssueDate())
                .expiryDate(d.getExpiryDate())
                .isExpired(d.getExpiryDate() != null &&
                        d.getExpiryDate().isBefore(LocalDate.now()))
                .fileUrl(d.getFileUrl())
                .isVerified(d.getIsVerified())
                .remarks(d.getRemarks())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private DocumentResponse mapToDocumentResponse(VehicleDocument d) {
        return DocumentResponse.builder()
                .id(d.getId())
                .documentTypeId(d.getDocumentType().getId())
                .documentTypeName(d.getDocumentType().getName())
                .documentNumber(d.getDocumentNumber())
                .issueDate(d.getIssueDate())
                .expiryDate(d.getExpiryDate())
                .isExpired(d.getExpiryDate() != null &&
                        d.getExpiryDate().isBefore(LocalDate.now()))
                .fileUrl(d.getFileUrl())
                .isVerified(d.getIsVerified())
                .remarks(d.getRemarks())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}