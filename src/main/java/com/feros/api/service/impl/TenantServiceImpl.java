package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.CreateTenantRequest;
import com.feros.api.dto.request.TenantSettingsUpdateRequest;
import com.feros.api.dto.request.UpdateMyTenantRequest;
import com.feros.api.dto.response.TenantSettingsResponse;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.dto.response.TenantDocumentResponse;
import com.feros.api.dto.response.TenantResponse;
import com.feros.api.entity.Designation;
import com.feros.api.entity.SubscriptionHistory;
import com.feros.api.entity.SubscriptionPlan;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.User;
import com.feros.api.entity.UserSession;
import com.feros.api.enums.DeviceType;
import com.feros.api.repository.UserRepository;
import com.feros.api.repository.UserSessionRepository;
import com.feros.api.entity.TenantDocument;
import com.feros.api.entity.master.*;
import com.feros.api.enums.PayCycle;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.SubscriptionStatus;
import com.feros.api.repository.SubscriptionHistoryRepository;
import com.feros.api.repository.SubscriptionPlanRepository;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.repository.TenantDocumentRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.service.S3Service;
import com.feros.api.service.TenantService;
import com.feros.api.util.JwtUtil;
import com.feros.api.util.SecurityUtil;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantDocumentRepository tenantDocumentRepository;
    private final ClientTypeRepository clientTypeRepository;
    private final PaymentTermsRepository paymentTermsRepository;
    private final ChargeTypeRepository chargeTypeRepository;
    private final DesignationRepository designationRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final S3Service s3Service;
    private final JwtUtil jwtUtil;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public TenantResponse createTenant(CreateTenantRequest request) {

        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new FerosException("Email already exists", HttpStatus.CONFLICT);
        }
        if (tenantRepository.existsByPhone(request.getPhone())) {
            throw new FerosException("Phone already exists", HttpStatus.CONFLICT);
        }

        Tenant tenant = Tenant.builder()
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .gstin(request.getGstin())
                .panNumber(request.getPanNumber())
                .tanNumber(request.getTanNumber())
                .cinNumber(request.getCinNumber())
                .transportLicenseNumber(request.getTransportLicenseNumber())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .branchName(request.getBranchName())
                .accountHolderName(request.getAccountHolderName())
                .ownerName(request.getOwnerName())
                .ownerPhone(request.getOwnerPhone())
                .ownerEmail(request.getOwnerEmail())
                .lorryCount(0)
                .subscriptionStatus(SubscriptionStatus.TRIAL)
                .trialStartDate(TimeUtil.today())
                .trialEndDate(TimeUtil.today().plusMonths(1))
                .isActive(true)
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);
        seedDefaultMasterData(savedTenant);
        createTrialHistory(savedTenant);
        return mapToResponse(savedTenant);
    }

    @Override
    public TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return mapToResponse(tenant);
    }

    @Override
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TenantResponse updateTenant(Long id, CreateTenantRequest request) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        tenant.setCompanyName(request.getCompanyName());
        if (request.getPrefix() != null) tenant.setPrefix(request.getPrefix());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setAddress(request.getAddress());
        tenant.setCity(request.getCity());
        tenant.setState(request.getState());
        tenant.setPincode(request.getPincode());
        tenant.setGstin(request.getGstin());
        tenant.setPanNumber(request.getPanNumber());
        tenant.setTanNumber(request.getTanNumber());
        tenant.setCinNumber(request.getCinNumber());
        tenant.setTransportLicenseNumber(request.getTransportLicenseNumber());
        tenant.setBankName(request.getBankName());
        tenant.setAccountNumber(request.getAccountNumber());
        tenant.setIfscCode(request.getIfscCode());
        tenant.setBranchName(request.getBranchName());
        tenant.setAccountHolderName(request.getAccountHolderName());
        tenant.setOwnerName(request.getOwnerName());
        tenant.setOwnerPhone(request.getOwnerPhone());
        tenant.setOwnerEmail(request.getOwnerEmail());

        return mapToResponse(tenantRepository.save(tenant));
    }

    @Override
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        tenant.setIsActive(false);
        tenantRepository.save(tenant);
    }

    @Override
    public BulkTenantUploadResponse bulkUpload(MultipartFile file) {
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();
        int rowNum = 1;

        try (CSVReader csvReader = new CSVReader(
                new InputStreamReader(file.getInputStream()))) {

            csvReader.readNext(); // skip header

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowNum++;
                try {
                    if (row.length < 12) {
                        errors.add("Row " + rowNum + ": Insufficient columns");
                        failureCount++;
                        continue;
                    }

                    String companyName = row[0].trim();
                    String email      = row[1].trim();
                    String phone      = row[2].trim();
                    String address    = row[3].trim();
                    String city       = row[4].trim();
                    String state      = row[5].trim();
                    String pincode    = row[6].trim();
                    String gstin      = row[7].trim();
                    String panNumber  = row[8].trim();
                    String ownerName  = row[9].trim();
                    String ownerPhone = row[10].trim();
                    String ownerEmail = row[11].trim();

                    if (tenantRepository.existsByEmail(email)) {
                        errors.add("Row " + rowNum + ": Email " + email + " already exists");
                        failureCount++;
                        continue;
                    }
                    if (tenantRepository.existsByPhone(phone)) {
                        errors.add("Row " + rowNum + ": Phone " + phone + " already exists");
                        failureCount++;
                        continue;
                    }

                    Tenant tenant = Tenant.builder()
                            .companyName(companyName)
                            .email(email).phone(phone)
                            .address(address).city(city)
                            .state(state).pincode(pincode)
                            .gstin(gstin).panNumber(panNumber)
                            .ownerName(ownerName).ownerPhone(ownerPhone)
                            .ownerEmail(ownerEmail).lorryCount(0)
                            .subscriptionStatus(SubscriptionStatus.TRIAL)
                            .trialStartDate(TimeUtil.today())
                            .trialEndDate(TimeUtil.today().plusMonths(1))
                            .isActive(true).build();

                    Tenant savedTenant = tenantRepository.save(tenant);
                    seedDefaultMasterData(savedTenant);
                    createTrialHistory(savedTenant);
                    successCount++;

                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                    failureCount++;
                }
            }

        } catch (Exception e) {
            throw new FerosException("Failed to parse CSV: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return BulkTenantUploadResponse.builder()
                .totalRows(rowNum - 1)
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();
    }

    // ===================== S3 — LOGO =====================

    @Override
    public TenantResponse uploadLogo(Long tenantId, MultipartFile file) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return doUploadLogo(tenant, file);
    }

    @Override
    public TenantResponse uploadMyLogo(MultipartFile file) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return doUploadLogo(tenant, file);
    }

    private TenantResponse doUploadLogo(Tenant tenant, MultipartFile file) {
        try {
            String prefix = tenant.getPrefix() != null ? tenant.getPrefix() : "T" + tenant.getId();
            String ext = getExtension(file.getOriginalFilename());
            String key = "tenants/images/" + prefix + "_logo" + ext;
            s3Service.uploadFileWithKey(file, key);
            tenant.setLogoUrl(key);
            return mapToResponse(tenantRepository.save(tenant));
        } catch (Exception e) {
            throw new FerosException("Failed to upload logo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== S3 — DOCUMENTS =====================

    @Override
    public TenantDocumentResponse addDocument(Long tenantId, String documentName, MultipartFile file) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return doAddDocument(tenant, documentName, file);
    }

    @Override
    public TenantDocumentResponse addMyDocument(String documentName, MultipartFile file) {
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return doAddDocument(tenant, documentName, file);
    }

    private TenantDocumentResponse doAddDocument(Tenant tenant, String documentName, MultipartFile file) {
        try {
            String prefix = tenant.getPrefix() != null ? tenant.getPrefix() : "T" + tenant.getId();
            String ext = getExtension(file.getOriginalFilename());
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String key = "tenants/docs/" + prefix + "_" + uuid + ext;
            s3Service.uploadFileWithKey(file, key);

            TenantDocument doc = TenantDocument.builder()
                    .tenant(tenant)
                    .documentName(documentName)
                    .originalFileName(file.getOriginalFilename())
                    .s3Key(key)
                    .isActive(true)
                    .build();
            TenantDocument saved = tenantDocumentRepository.save(doc);
            return mapDocToResponse(saved);
        } catch (Exception e) {
            throw new FerosException("Failed to upload document: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<TenantDocumentResponse> getDocuments(Long tenantId) {
        return tenantDocumentRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream().map(this::mapDocToResponse).toList();
    }

    @Override
    public List<TenantDocumentResponse> getMyDocuments() {
        return tenantDocumentRepository.findByTenantIdAndIsActiveTrue(SecurityUtil.getCurrentTenantId())
                .stream().map(this::mapDocToResponse).toList();
    }

    @Override
    public void deleteDocument(Long tenantId, Long docId) {
        TenantDocument doc = tenantDocumentRepository.findByIdAndIsActiveTrue(docId)
                .orElseThrow(() -> new FerosException("Document not found", HttpStatus.NOT_FOUND));
        if (!doc.getTenant().getId().equals(tenantId))
            throw new FerosException("Document does not belong to this tenant", HttpStatus.FORBIDDEN);
        doDeleteDocument(doc);
    }

    @Override
    public void deleteMyDocument(Long docId) {
        TenantDocument doc = tenantDocumentRepository.findByIdAndIsActiveTrue(docId)
                .orElseThrow(() -> new FerosException("Document not found", HttpStatus.NOT_FOUND));
        if (!doc.getTenant().getId().equals(SecurityUtil.getCurrentTenantId()))
            throw new FerosException("Document does not belong to your tenant", HttpStatus.FORBIDDEN);
        doDeleteDocument(doc);
    }

    private void doDeleteDocument(TenantDocument doc) {
        s3Service.deleteFile(doc.getS3Key());
        doc.setIsActive(false);
        tenantDocumentRepository.save(doc);
    }

    private TenantDocumentResponse mapDocToResponse(TenantDocument doc) {
        return TenantDocumentResponse.builder()
                .id(doc.getId())
                .documentName(doc.getDocumentName())
                .originalFileName(doc.getOriginalFileName())
                .s3Key(doc.getS3Key())
                .fileUrl(s3Service.generatePresignedUrl(doc.getS3Key()))
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    // ===================== SEED DEFAULT MASTER DATA =====================
    private void seedDefaultMasterData(Tenant tenant) {

        // Client Types
        List.of("Regular", "Spot", "Contract", "Government")
                .forEach(name -> clientTypeRepository.save(
                        ClientType.builder().tenant(tenant).name(name).isActive(true).build()));

        // Payment Terms
        Map<String, Integer> paymentTerms = new LinkedHashMap<>();
        paymentTerms.put("Immediate", 0);
        paymentTerms.put("Net 15", 15);
        paymentTerms.put("Net 30", 30);
        paymentTerms.put("Net 45", 45);
        paymentTerms.put("Net 60", 60);
        paymentTerms.forEach((name, days) -> paymentTermsRepository.save(
                PaymentTerms.builder().tenant(tenant).name(name)
                        .creditDays(days).isActive(true).build()));

        // Charge Types
        Map<String, String> chargeTypes = new LinkedHashMap<>();
        chargeTypes.put("Loading Charge", "Charge for loading goods at source");
        chargeTypes.put("Unloading Charge", "Charge for unloading goods at destination");
        chargeTypes.put("Detention Charge", "Charge for vehicle detention beyond allowed time");
        chargeTypes.put("Checkpost Fine", "Fines paid at checkposts during transit");
        chargeTypes.put("Toll Charge", "Toll tax paid during transit");
        chargeTypes.put("Halting Charge", "Charge for vehicle halting overnight");
        chargeTypes.forEach((name, desc) -> chargeTypeRepository.save(
                ChargeType.builder().tenant(tenant).name(name)
                        .description(desc).isActive(true).build()));

        // Designations
        Map<String, RoleName> designations = new LinkedHashMap<>();
        designations.put("Senior Driver", RoleName.DRIVER);
        designations.put("Junior Driver", RoleName.DRIVER);
        designations.put("Senior Cleaner", RoleName.CLEANER);
        designations.put("Junior Cleaner", RoleName.CLEANER);
        designations.put("Supervisor", RoleName.SUPERVISOR);
        designations.put("Office Staff", RoleName.OFFICE_STAFF);
        designations.forEach((name, role) -> designationRepository.save(
                Designation.builder().tenant(tenant).name(name)
                        .roleType(role).isActive(true).build()));

        // Tenant Settings
        tenantSettingsRepository.save(TenantSettings.builder()
                .tenant(tenant)
                .payCycle(PayCycle.MONTHLY)
                .overtimeThresholdHours(BigDecimal.valueOf(8.00))
                .overtimeRateMultiplier(BigDecimal.valueOf(1.50))
                .maxAdvanceAmount(BigDecimal.valueOf(5000))
                .maxAdvanceDeductionPerCycle(BigDecimal.valueOf(2000))
                .isTripBonusEnabled(false)
                .tripBonusAmount(BigDecimal.ZERO)
                .attendanceEnforced(false)
                .attendanceDeadlineTime(LocalTime.of(8, 0))
                .isActive(true)
                .build());
    }

    // ===================== MY TENANT (ADMIN/OFFICE_STAFF) =====================
    @Override
    public TenantResponse getMyTenant() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
        return mapToResponse(tenant);
    }

    @Override
    public TenantResponse updateMyTenant(UpdateMyTenantRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        // Check email uniqueness excluding self
        if (!tenant.getEmail().equals(request.getEmail()) && tenantRepository.existsByEmail(request.getEmail())) {
            throw new FerosException("Email already in use", HttpStatus.CONFLICT);
        }
        // Check phone uniqueness excluding self
        if (!tenant.getPhone().equals(request.getPhone()) && tenantRepository.existsByPhone(request.getPhone())) {
            throw new FerosException("Phone already in use", HttpStatus.CONFLICT);
        }

        tenant.setCompanyName(request.getCompanyName());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setAddress(request.getAddress());
        tenant.setCity(request.getCity());
        tenant.setState(request.getState());
        tenant.setPincode(request.getPincode());
        tenant.setGstin(request.getGstin());
        tenant.setPanNumber(request.getPanNumber());
        tenant.setTanNumber(request.getTanNumber());
        tenant.setCinNumber(request.getCinNumber());
        tenant.setTransportLicenseNumber(request.getTransportLicenseNumber());
        tenant.setBankName(request.getBankName());
        tenant.setAccountNumber(request.getAccountNumber());
        tenant.setIfscCode(request.getIfscCode());
        tenant.setBranchName(request.getBranchName());
        tenant.setAccountHolderName(request.getAccountHolderName());
        tenant.setOwnerName(request.getOwnerName());
        tenant.setOwnerPhone(request.getOwnerPhone());
        tenant.setOwnerEmail(request.getOwnerEmail());

        return mapToResponse(tenantRepository.save(tenant));
    }

    // ===================== SETTINGS =====================

    @Override
    public TenantSettingsResponse getSettings() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new FerosException("Settings not found", HttpStatus.NOT_FOUND));
        return mapSettingsToResponse(settings);
    }

    @Override
    public TenantSettingsResponse updateSettings(TenantSettingsUpdateRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new FerosException("Settings not found", HttpStatus.NOT_FOUND));

        if (request.getPayCycle() != null)                       settings.setPayCycle(com.feros.api.enums.PayCycle.valueOf(request.getPayCycle()));
        if (request.getOvertimeThresholdHours() != null)         settings.setOvertimeThresholdHours(request.getOvertimeThresholdHours());
        if (request.getOvertimeRateMultiplier() != null)         settings.setOvertimeRateMultiplier(request.getOvertimeRateMultiplier());
        if (request.getMaxAdvanceAmount() != null)               settings.setMaxAdvanceAmount(request.getMaxAdvanceAmount());
        if (request.getMaxAdvanceDeductionPerCycle() != null)    settings.setMaxAdvanceDeductionPerCycle(request.getMaxAdvanceDeductionPerCycle());
        if (request.getIsTripBonusEnabled() != null)             settings.setIsTripBonusEnabled(request.getIsTripBonusEnabled());
        if (request.getTripBonusAmount() != null)                settings.setTripBonusAmount(request.getTripBonusAmount());
        if (request.getAttendanceEnforced() != null)             settings.setAttendanceEnforced(request.getAttendanceEnforced());
        if (request.getAttendanceDeadlineTime() != null)         settings.setAttendanceDeadlineTime(request.getAttendanceDeadlineTime());
        if (request.getRequireTyreApproval() != null)            settings.setRequireTyreApproval(request.getRequireTyreApproval());
        if (request.getRequireSparePartApproval() != null)       settings.setRequireSparePartApproval(request.getRequireSparePartApproval());
        if (request.getServiceGstRate() != null)                 settings.setServiceGstRate(request.getServiceGstRate());

        return mapSettingsToResponse(tenantSettingsRepository.save(settings));
    }

    private TenantSettingsResponse mapSettingsToResponse(TenantSettings s) {
        return TenantSettingsResponse.builder()
                .id(s.getId())
                .tenantId(s.getTenant().getId())
                .payCycle(s.getPayCycle())
                .overtimeThresholdHours(s.getOvertimeThresholdHours())
                .overtimeRateMultiplier(s.getOvertimeRateMultiplier())
                .maxAdvanceAmount(s.getMaxAdvanceAmount())
                .maxAdvanceDeductionPerCycle(s.getMaxAdvanceDeductionPerCycle())
                .isTripBonusEnabled(s.getIsTripBonusEnabled())
                .tripBonusAmount(s.getTripBonusAmount())
                .attendanceEnforced(s.getAttendanceEnforced())
                .attendanceDeadlineTime(s.getAttendanceDeadlineTime())
                .requireTyreApproval(s.getRequireTyreApproval())
                .requireSparePartApproval(s.getRequireSparePartApproval())
                .serviceGstRate(s.getServiceGstRate())
                .build();
    }

    // ===================== IMPERSONATION =====================
    @Override
    public LoginResponse impersonateTenant(Long tenantId, Long saUserId, String saPhone) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        String token = jwtUtil.generateToken(saUserId, tenant.getId(), saPhone, "ADMIN");

        // Save impersonation session so JwtAuthenticationFilter can validate it
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        UserSession session = userSessionRepository
                .findByUserIdAndDeviceType(saUserId, DeviceType.WEB)
                .orElse(UserSession.builder()
                        .user(userRepository.getReferenceById(saUserId))
                        .deviceType(DeviceType.WEB)
                        .loggedInAt(now)
                        .build());
        session.setToken(token);
        session.setLoggedInAt(now);
        session.setLastActiveAt(now);
        session.setLoggedOutAt(null);
        userSessionRepository.save(session);

        return LoginResponse.builder()
                .token(token)
                .userId(saUserId)
                .name("Super Admin")
                .phone(saPhone)
                .role("ADMIN")
                .tenantId(tenant.getId())
                .companyName(tenant.getCompanyName())
                .logoUrl(tenant.getLogoUrl() != null ? s3Service.getPublicUrl(tenant.getLogoUrl()) : null)
                .isPinResetRequired(false)
                .build();
    }

    // ===================== MAPPER =====================
    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .companyName(tenant.getCompanyName())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .address(tenant.getAddress())
                .city(tenant.getCity())
                .state(tenant.getState())
                .pincode(tenant.getPincode())
                .gstin(tenant.getGstin())
                .panNumber(tenant.getPanNumber())
                .tanNumber(tenant.getTanNumber())
                .cinNumber(tenant.getCinNumber())
                .transportLicenseNumber(tenant.getTransportLicenseNumber())
                .bankName(tenant.getBankName())
                .accountNumber(tenant.getAccountNumber())
                .ifscCode(tenant.getIfscCode())
                .branchName(tenant.getBranchName())
                .accountHolderName(tenant.getAccountHolderName())
                .ownerName(tenant.getOwnerName())
                .ownerPhone(tenant.getOwnerPhone())
                .ownerEmail(tenant.getOwnerEmail())
                .prefix(tenant.getPrefix())
                .logoUrl(tenant.getLogoUrl() != null ? s3Service.getPublicUrl(tenant.getLogoUrl()) : null)
                .lorryCount(tenant.getLorryCount())
                .subscriptionStatus(tenant.getSubscriptionStatus())
                .trialStartDate(tenant.getTrialStartDate())
                .trialEndDate(tenant.getTrialEndDate())
                .subscriptionStartDate(tenant.getSubscriptionStartDate())
                .subscriptionEndDate(tenant.getSubscriptionEndDate())
                .isActive(tenant.getIsActive())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    private void createTrialHistory(Tenant tenant) {
        LocalDate start = TimeUtil.today();
        LocalDate end   = start.plusDays(30);
        SubscriptionPlan trialPlan = subscriptionPlanRepository.findByNameIgnoreCase("Trial").orElse(null);
        SubscriptionHistory trial = SubscriptionHistory.builder()
                .tenant(tenant)
                .plan(trialPlan)
                .status(SubscriptionStatus.TRIAL)
                .startDate(start)
                .endDate(end)
                .amount(BigDecimal.ZERO)
                .gstAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .notes("30-day free trial — auto-created on signup")
                .createdAt(TimeUtil.nowIst())
                .build();
        subscriptionHistoryRepository.save(trial);
    }
}