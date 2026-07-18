package com.feros.api.service.impl;

import com.feros.api.util.TimeUtil;
import com.feros.api.dto.request.CreateUserRequest;
import com.feros.api.dto.request.UserStatusRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.PinResponse;
import com.feros.api.dto.response.UserResponse;
import com.feros.api.entity.Role;
import com.feros.api.entity.StaffProfile;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.User;
import com.feros.api.entity.master.City;
import com.feros.api.entity.master.EmploymentType;
import com.feros.api.entity.master.State;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.StaffAllocationStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.enums.AttendanceApprovalStatus;
import com.feros.api.repository.*;
import com.feros.api.service.NotificationService;
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.service.UserService;
import com.feros.api.enums.NotificationType;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffProfileRepository staffProfileRepository;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final OrderStaffAllocationRepository orderStaffAllocationRepository;
    private final NotificationService notificationService;
    private final AttendanceRepository attendanceRepository;
    private final NumberGeneratorService numberGenerator;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        // 1. Resolve tenantId
        Long tenantId = resolveTenantId(request.getTenantId());

        // 2. Check duplicate phone
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new FerosException("Phone number already exists", HttpStatus.CONFLICT);
        }

        // 3. Get tenant
        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException(
                        "Tenant not found", HttpStatus.NOT_FOUND));


        // 4. Get role
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new FerosException(
                        "Role not found", HttpStatus.NOT_FOUND));

        // 5. Generate PIN
        String rawPin = generatePin();
        String hashedPin = passwordEncoder.encode(rawPin);

        // 6. Create user
        User user = User.builder()
                .tenant(tenant)
                .userNumber(numberGenerator.generateSequential(tenant.getId(), NumberUtil.Type.USR))
                .name(request.getName())
                .phone(request.getPhone())
                .pin(hashedPin)
                .plainPin(rawPin)
                .pinGeneratedAt(TimeUtil.nowIst())
                .isPinResetRequired(true)
                .isActive(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();

        User savedUser = userRepository.save(user);

        // 7. Create staff profile for DRIVER, CLEANER, SUPERVISOR
        if (isStaffRole(request.getRole())) {
            createStaffProfile(savedUser, tenant, request);
        }

        return mapToResponse(savedUser, rawPin);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException(
                        "User not found", HttpStatus.NOT_FOUND));
        validateTenantAccess(user);
        return mapToResponse(user, null);
    }

    @Override
    public List<UserResponse> getAllUsers(Boolean hasAttendanceToday) {
        List<User> users;
        if (SecurityUtil.isSuperAdmin()) {
            users = userRepository.findAll();
        } else {
            Long tenantId = SecurityUtil.getCurrentTenantId();
            users = userRepository.findAllByTenantId(tenantId);

            if (Boolean.TRUE.equals(hasAttendanceToday)) {
                List<AttendanceApprovalStatus> validStatuses = List.of(
                        AttendanceApprovalStatus.PENDING, AttendanceApprovalStatus.APPROVED);
                List<Long> presentUserIds = attendanceRepository.findUserIdsWithAttendanceOnDate(
                        tenantId, TimeUtil.today(), validStatuses);
                users = users.stream()
                        .filter(u -> presentUserIds.contains(u.getId()))
                        .toList();
            }
        }

        if (users.isEmpty()) return List.of();

        // Bulk-load all 3 datasets in 3 queries instead of 3 per user
        List<Long> userIds = users.stream().map(User::getId).toList();

        List<StaffAllocationStatus> activeStatuses = List.of(
                StaffAllocationStatus.ALLOCATED, StaffAllocationStatus.IN_TRANSIT);

        Map<Long, Long> completedCounts = orderStaffAllocationRepository
                .countCompletedByUserIds(userIds, StaffAllocationStatus.COMPLETED)
                .stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        Map<Long, com.feros.api.entity.OrderStaffAllocation> activeAllocByUser =
                orderStaffAllocationRepository
                        .findActiveAllocationsByUserIds(userIds, activeStatuses)
                        .stream()
                        .collect(Collectors.toMap(
                                sa -> sa.getUser().getId(),
                                sa -> sa,
                                (a, b) -> a)); // keep first (ordered DESC)

        Map<Long, StaffProfile> profileByUser = staffProfileRepository
                .findByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

        return users.stream()
                .map(u -> mapToResponseBulk(u, completedCounts, activeAllocByUser, profileByUser))
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException(
                        "User not found", HttpStatus.NOT_FOUND));
        validateTenantAccess(user);

        user.setName(request.getName());
        user.setPhone(request.getPhone());

        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new FerosException("Role not found", HttpStatus.NOT_FOUND));
            user.setRoles(new HashSet<>(Set.of(role)));
        }

        User updated = userRepository.save(user);

        if (isStaffRole(request.getRole())) {
            staffProfileRepository.findByUserId(id).ifPresent(profile -> {
                updateStaffProfile(profile, request);
                staffProfileRepository.save(profile);
            });
        }

        return mapToResponse(updated, null);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException(
                        "User not found", HttpStatus.NOT_FOUND));
        validateTenantAccess(user);
        List<StaffAllocationStatus> activeStatuses = List.of(StaffAllocationStatus.ALLOCATED, StaffAllocationStatus.IN_TRANSIT);
        if (!orderStaffAllocationRepository.findActiveAllocationsForUser(id, activeStatuses).isEmpty()) {
            throw new FerosException(
                    "Cannot delete staff — they are currently assigned to an active order. Unassign them first.",
                    HttpStatus.CONFLICT);
        }
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse toggleUserStatus(Long userId, UserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException(
                        "User not found", HttpStatus.NOT_FOUND));
        validateTenantAccess(user);
        // Block deactivation if staff is on an active allocation
        List<StaffAllocationStatus> activeStatuses = List.of(StaffAllocationStatus.ALLOCATED, StaffAllocationStatus.IN_TRANSIT);
        if (Boolean.FALSE.equals(request.getIsActive()) &&
                !orderStaffAllocationRepository.findActiveAllocationsForUser(userId, activeStatuses).isEmpty()) {
            throw new FerosException(
                    "Cannot deactivate staff — they are currently assigned to an active order. Unassign them first.",
                    HttpStatus.CONFLICT);
        }
        user.setIsActive(request.getIsActive());
        User updated = userRepository.save(user);
        return mapToResponse(updated, null);
    }

    @Override
    @Transactional
    public PinResponse resetPin(Long id) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException(
                        "User not found", HttpStatus.NOT_FOUND));
        validateTenantAccess(user);

        String rawPin = generatePin();
        user.setPin(passwordEncoder.encode(rawPin));
        user.setPlainPin(rawPin);
        user.setPinGeneratedAt(TimeUtil.nowIst());
        user.setIsPinResetRequired(true);
        // Clear any active lockout so user can login immediately with new PIN
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        notificationService.sendToUser(user.getTenant(), user, NotificationType.PIN_RESET,
                "PIN Reset",
                "Your login PIN has been reset by admin. Please use your new PIN to login.");

        return PinResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .pin(rawPin)
                .message("PIN reset successfully. Share this PIN with the user.")
                .build();
    }

    @Override
    public BulkTenantUploadResponse bulkUpload(MultipartFile file, Long tenantId) {
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();
        int rowNum = 1;

        Long resolvedTenantId = resolveTenantId(tenantId);

        try (CSVReader csvReader = new CSVReader(
                new InputStreamReader(file.getInputStream()))) {

            csvReader.readNext();

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowNum++;
                try {
                    if (row.length < 3) {
                        errors.add("Row " + rowNum + ": Insufficient columns");
                        failureCount++;
                        continue;
                    }

                    String name = row[0].trim();
                    String phone = row[1].trim();
                    String roleName = row[2].trim();

                    if (userRepository.existsByPhone(phone)) {
                        errors.add("Row " + rowNum + ": Phone " + phone + " already exists");
                        failureCount++;
                        continue;
                    }

                    RoleName role = RoleName.valueOf(roleName.toUpperCase());
                    Role roleEntity = roleRepository.findByName(role)
                            .orElseThrow(() -> new FerosException(
                                    "Role not found", HttpStatus.NOT_FOUND));

                    Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(resolvedTenantId)
                            .orElseThrow(() -> new FerosException(
                                    "Tenant not found", HttpStatus.NOT_FOUND));

                    String rawPin = generatePin();
                    String hashedPin = passwordEncoder.encode(rawPin);

                    User user = User.builder()
                            .tenant(tenant)
                            .userNumber(numberGenerator.generateSequential(tenant.getId(), NumberUtil.Type.USR))
                            .name(name)
                            .phone(phone)
                            .pin(hashedPin)
                            .plainPin(rawPin)
                            .pinGeneratedAt(TimeUtil.nowIst())
                            .isPinResetRequired(true)
                            .isActive(true)
                            .roles(new HashSet<>(Set.of(roleEntity)))
                            .build();

                    userRepository.save(user);
                    successCount++;

                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                    failureCount++;
                }
            }

        } catch (Exception e) {
            throw new FerosException("Failed to parse CSV: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }

        return BulkTenantUploadResponse.builder()
                .totalRows(rowNum - 1)
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public BulkTenantUploadResponse staffBulkUpload(MultipartFile file, Long tenantId) {
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();
        int rowNum = 1;

        Long resolvedTenantId = resolveTenantId(tenantId);

        // Get first available employment type for default profile creation
        EmploymentType defaultEmploymentType = employmentTypeRepository.findAllByIsActiveTrue()
                .stream().findFirst().orElse(null);

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            csvReader.readNext(); // skip header

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowNum++;
                try {
                    if (row.length < 3) {
                        errors.add("Row " + rowNum + ": Insufficient columns (name, phone, role required)");
                        failureCount++;
                        continue;
                    }

                    String name            = row[0].trim();
                    String phone           = row[1].trim();
                    String roleName        = row[2].trim();
                    String joiningDateStr  = row.length > 3 ? row[3].trim() : "";
                    String licenseNumber   = row.length > 4 ? row[4].trim() : "";
                    String licenseExpiryStr= row.length > 5 ? row[5].trim() : "";

                    if (userRepository.existsByPhone(phone)) {
                        errors.add("Row " + rowNum + ": Phone " + phone + " already exists");
                        failureCount++;
                        continue;
                    }

                    RoleName role = RoleName.valueOf(roleName.toUpperCase());
                    Role roleEntity = roleRepository.findByName(role)
                            .orElseThrow(() -> new FerosException("Role not found", HttpStatus.NOT_FOUND));

                    Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(resolvedTenantId)
                            .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

                    String rawPin = generatePin();
                    User user = User.builder()
                            .tenant(tenant)
                            .userNumber(numberGenerator.generateSequential(tenant.getId(), NumberUtil.Type.USR))
                            .name(name)
                            .phone(phone)
                            .pin(passwordEncoder.encode(rawPin))
                            .plainPin(rawPin)
                            .pinGeneratedAt(TimeUtil.nowIst())
                            .isPinResetRequired(true)
                            .isActive(true)
                            .roles(new HashSet<>(Set.of(roleEntity)))
                            .build();

                    userRepository.save(user);

                    // Create staff profile if employment type is available
                    if (defaultEmploymentType != null) {
                        StaffProfile.StaffProfileBuilder profileBuilder = StaffProfile.builder()
                                .user(user)
                                .tenant(tenant)
                                .employmentType(defaultEmploymentType)
                                .isActive(true);

                        if (!joiningDateStr.isEmpty()) {
                            profileBuilder.joiningDate(java.time.LocalDate.parse(joiningDateStr));
                        }
                        if (!licenseNumber.isEmpty()) {
                            profileBuilder.licenseNumber(licenseNumber);
                        }
                        if (!licenseExpiryStr.isEmpty()) {
                            profileBuilder.licenseExpiryDate(java.time.LocalDate.parse(licenseExpiryStr));
                        }

                        staffProfileRepository.save(profileBuilder.build());
                    }

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

    /**
     * Import staff full details (bank / aadhar / nominee) with UPSERT by phone.
     * Existing phone  -> update user name + profile fields (blank cells never overwrite).
     * New phone       -> create user (auto PIN + number) + profile.
     * CSV column order (14): name, role, bankName, branchName, accountNumber, ifscCode,
     *   aadharNumber, aadharName, dateOfBirth, nomineeName, nomineeRelation,
     *   nomineeDateOfBirth, nomineeAadharNumber, phone. Dates: yyyy-MM-dd.
     */
    @Override
    @Transactional
    public BulkTenantUploadResponse staffDetailsImport(MultipartFile file, Long tenantId) {
        int successCount = 0, failureCount = 0, rowNum = 1;
        List<String> errors = new ArrayList<>();

        Long resolvedTenantId = resolveTenantId(tenantId);
        EmploymentType defaultEmploymentType = employmentTypeRepository.findAllByIsActiveTrue()
                .stream().findFirst().orElse(null);

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            csvReader.readNext(); // skip header
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowNum++;
                try {
                    String phone = cell(row, 13);
                    String name  = cell(row, 0);
                    String roleName = cell(row, 1);
                    if (phone.isEmpty() || name.isEmpty() || roleName.isEmpty()) {
                        errors.add("Row " + rowNum + ": name, phone and role are required");
                        failureCount++;
                        continue;
                    }

                    Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(resolvedTenantId)
                            .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

                    User user = userRepository.findByPhone(phone).orElse(null);
                    if (user == null) {
                        RoleName role = RoleName.valueOf(roleName.toUpperCase());
                        Role roleEntity = roleRepository.findByName(role)
                                .orElseThrow(() -> new FerosException("Role not found: " + roleName, HttpStatus.NOT_FOUND));
                        String rawPin = generatePin();
                        user = userRepository.save(User.builder()
                                .tenant(tenant)
                                .userNumber(numberGenerator.generateSequential(tenant.getId(), NumberUtil.Type.USR))
                                .name(name)
                                .phone(phone)
                                .pin(passwordEncoder.encode(rawPin))
                                .plainPin(rawPin)
                                .pinGeneratedAt(TimeUtil.nowIst())
                                .isPinResetRequired(true)
                                .isActive(true)
                                .roles(new HashSet<>(Set.of(roleEntity)))
                                .build());
                    } else {
                        user.setName(name); // name is required, always present
                        userRepository.save(user);
                    }

                    StaffProfile profile = staffProfileRepository.findByUserId(user.getId()).orElse(null);
                    if (profile == null) {
                        if (defaultEmploymentType == null) {
                            errors.add("Row " + rowNum + ": no employment type configured for new profile");
                            failureCount++;
                            continue;
                        }
                        profile = StaffProfile.builder()
                                .user(user).tenant(tenant)
                                .employmentType(defaultEmploymentType)
                                .isActive(true).build();
                    }

                    // blank cells never overwrite
                    applyIfPresent(cell(row, 2),  profile::setBankName);
                    applyIfPresent(cell(row, 3),  profile::setBankBranchName);
                    applyIfPresent(cell(row, 4),  profile::setAccountNumber);
                    applyIfPresent(cell(row, 5),  profile::setIfscCode);
                    applyIfPresent(cell(row, 6),  profile::setAadharNumber);
                    applyIfPresent(cell(row, 7),  profile::setAadharName);
                    applyIfPresent(cell(row, 7),  profile::setAccountHolderName); // aadhar name = account holder
                    applyIfPresent(cell(row, 9),  profile::setNomineeName);
                    applyIfPresent(cell(row, 10), profile::setNomineeRelation);
                    applyIfPresent(cell(row, 12), profile::setNomineeAadharNumber);
                    LocalDate dob = parseDate(cell(row, 8));
                    if (dob != null) profile.setDateOfBirth(dob);
                    LocalDate nomDob = parseDate(cell(row, 11));
                    if (nomDob != null) profile.setNomineeDateOfBirth(nomDob);

                    staffProfileRepository.save(profile);
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

    private static String cell(String[] row, int i) {
        return (i < row.length && row[i] != null) ? row[i].trim() : "";
    }

    private static void applyIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.isEmpty()) setter.accept(value);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDate.parse(value); // ponytail: cleaned CSV is ISO yyyy-MM-dd; bad values just skip
        } catch (Exception e) {
            return null;
        }
    }

    // Helper methods
    private String generatePin() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    private boolean isStaffRole(RoleName role) {
        return role == RoleName.DRIVER ||
                role == RoleName.CLEANER ||
                role == RoleName.SUPERVISOR ||
                role == RoleName.OPERATOR;
    }

    private boolean resolveCanAccessVehicles(CreateUserRequest request, Tenant tenant) {
        if (tenant.getModuleType() == null) return true;
        return switch (tenant.getModuleType()) {
            case VEHICLES_ONLY  -> true;
            case EQUIPMENT_ONLY -> false;
            case BOTH           -> request.getRole() == RoleName.OPERATOR
                                   ? false
                                   : !Boolean.FALSE.equals(request.getCanAccessVehicles());
        };
    }

    private boolean resolveCanAccessEquipment(CreateUserRequest request, Tenant tenant) {
        if (tenant.getModuleType() == null) return false;
        return switch (tenant.getModuleType()) {
            case EQUIPMENT_ONLY -> true;
            case VEHICLES_ONLY  -> false;
            case BOTH           -> Boolean.TRUE.equals(request.getCanAccessEquipment())
                                   || request.getRole() == RoleName.OPERATOR;
        };
    }

    private Long resolveTenantId(Long requestTenantId) {
        if (SecurityUtil.isSuperAdmin()) {
            if (requestTenantId == null) {
                throw new FerosException(
                        "Tenant ID is required for SUPER_ADMIN",
                        HttpStatus.BAD_REQUEST);
            }
            return requestTenantId;
        }
        return SecurityUtil.getCurrentTenantId();
    }

    private void validateTenantAccess(User user) {
        if (!SecurityUtil.isSuperAdmin()) {
            Long currentTenantId = SecurityUtil.getCurrentTenantId();
            if (!user.getTenant().getId().equals(currentTenantId)) {
                throw new FerosException("Access denied", HttpStatus.FORBIDDEN);
            }
        }
    }

    private void createStaffProfile(User user, Tenant tenant,
            CreateUserRequest request) {
        StaffProfile profile = StaffProfile.builder()
                .user(user)
                .tenant(tenant)
                .dateOfBirth(request.getDateOfBirth())
                .joiningDate(request.getJoiningDate())
                .address(request.getAddress())
                .pincode(request.getPincode())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .accountHolderName(request.getAccountHolderName())
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiryDate(request.getLicenseExpiryDate())
                .salaryType(request.getSalaryType() != null ? request.getSalaryType() : com.feros.api.enums.SalaryType.MONTHLY)
                .monthlySalary(request.getMonthlySalary())
                .isActive(true)
                .canAccessVehicles(resolveCanAccessVehicles(request, tenant))
                .canAccessEquipment(resolveCanAccessEquipment(request, tenant))
                .build();

        if (request.getEmploymentTypeId() != null) {
            EmploymentType employmentType = employmentTypeRepository
                    .findById(request.getEmploymentTypeId())
                    .orElseThrow(() -> new FerosException(
                            "Employment type not found", HttpStatus.NOT_FOUND));
            profile.setEmploymentType(employmentType);
        }

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new FerosException(
                            "City not found", HttpStatus.NOT_FOUND));
            profile.setCity(city);
        }

        if (request.getStateId() != null) {
            State state = stateRepository.findById(request.getStateId())
                    .orElseThrow(() -> new FerosException(
                            "State not found", HttpStatus.NOT_FOUND));
            profile.setState(state);
        }

        staffProfileRepository.save(profile);
    }

    private void updateStaffProfile(StaffProfile profile,
            CreateUserRequest request) {
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
    }

    private UserResponse mapToResponse(User user, String rawPin) {
        String pinToReturn = rawPin != null ? rawPin : user.getPlainPin();
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .userNumber(user.getUserNumber())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRoles().stream()
                        .map(Role::getName)
                        .findFirst()
                        .orElse(null))
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .companyName(user.getTenant() != null ? user.getTenant().getCompanyName() : "FEROS")
                .isActive(user.getIsActive())
                .isPinResetRequired(user.getIsPinResetRequired())
                .generatedPin(pinToReturn)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        response.setCompletedTripsCount(
                orderStaffAllocationRepository.countByUserIdAndAllocationStatusAndIsActiveTrue(
                        user.getId(), StaffAllocationStatus.COMPLETED));

        List<StaffAllocationStatus> driverActiveStatuses = List.of(StaffAllocationStatus.ALLOCATED, StaffAllocationStatus.IN_TRANSIT);
        List<com.feros.api.entity.OrderStaffAllocation> activeAllocs =
                orderStaffAllocationRepository.findActiveAllocationsForUser(user.getId(), driverActiveStatuses);
        if (!activeAllocs.isEmpty()) {
            response.setIsAssigned(true);
            response.setActiveOrderNumber(activeAllocs.get(0).getOrder().getOrderNumber());
        } else {
            response.setIsAssigned(false);
        }

        staffProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            response.setDesignationName(
                    profile.getDesignation() != null ? profile.getDesignation().getName() : null);
            response.setEmploymentType(
                    profile.getEmploymentType() != null ? profile.getEmploymentType().getName() : null);
            response.setDateOfBirth(profile.getDateOfBirth());
            response.setJoiningDate(profile.getJoiningDate());
            response.setAddress(profile.getAddress());
            response.setCity(profile.getCity() != null ? profile.getCity().getName() : null);
            response.setState(profile.getState() != null ? profile.getState().getName() : null);
            response.setPincode(profile.getPincode());
            response.setEmergencyContactName(profile.getEmergencyContactName());
            response.setEmergencyContactPhone(profile.getEmergencyContactPhone());
            response.setBankName(profile.getBankName());
            response.setAccountNumber(profile.getAccountNumber());
            response.setIfscCode(profile.getIfscCode());
            response.setAccountHolderName(profile.getAccountHolderName());
            response.setLicenseNumber(profile.getLicenseNumber());
            response.setLicenseExpiryDate(profile.getLicenseExpiryDate());
            response.setProfilePhotoUrl(profile.getProfilePhotoUrl());
        });

        return response;
    }

    private UserResponse mapToResponseBulk(
            User user,
            Map<Long, Long> completedCounts,
            Map<Long, com.feros.api.entity.OrderStaffAllocation> activeAllocByUser,
            Map<Long, StaffProfile> profileByUser) {

        String pinToReturn = user.getPlainPin();
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .userNumber(user.getUserNumber())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRoles().stream().map(Role::getName).findFirst().orElse(null))
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .companyName(user.getTenant() != null ? user.getTenant().getCompanyName() : "FEROS")
                .isActive(user.getIsActive())
                .isPinResetRequired(user.getIsPinResetRequired())
                .generatedPin(pinToReturn)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        response.setCompletedTripsCount(completedCounts.getOrDefault(user.getId(), 0L));

        com.feros.api.entity.OrderStaffAllocation activeAlloc = activeAllocByUser.get(user.getId());
        if (activeAlloc != null) {
            response.setIsAssigned(true);
            response.setActiveOrderNumber(activeAlloc.getOrder().getOrderNumber());
        } else {
            response.setIsAssigned(false);
        }

        StaffProfile profile = profileByUser.get(user.getId());
        if (profile != null) {
            response.setDesignationName(profile.getDesignation() != null ? profile.getDesignation().getName() : null);
            response.setEmploymentType(profile.getEmploymentType() != null ? profile.getEmploymentType().getName() : null);
            response.setDateOfBirth(profile.getDateOfBirth());
            response.setJoiningDate(profile.getJoiningDate());
            response.setAddress(profile.getAddress());
            response.setCity(profile.getCity() != null ? profile.getCity().getName() : null);
            response.setState(profile.getState() != null ? profile.getState().getName() : null);
            response.setPincode(profile.getPincode());
            response.setEmergencyContactName(profile.getEmergencyContactName());
            response.setEmergencyContactPhone(profile.getEmergencyContactPhone());
            response.setBankName(profile.getBankName());
            response.setAccountNumber(profile.getAccountNumber());
            response.setIfscCode(profile.getIfscCode());
            response.setAccountHolderName(profile.getAccountHolderName());
            response.setLicenseNumber(profile.getLicenseNumber());
            response.setLicenseExpiryDate(profile.getLicenseExpiryDate());
            response.setProfilePhotoUrl(profile.getProfilePhotoUrl());
        }

        return response;
    }
}