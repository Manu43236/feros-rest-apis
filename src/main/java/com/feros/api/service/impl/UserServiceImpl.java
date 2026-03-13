package com.feros.api.service.impl;

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
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.UserService;
import com.feros.api.util.SecurityUtil;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
                .name(request.getName())
                .phone(request.getPhone())
                .pin(hashedPin)
                .plainPin(rawPin)
                .pinGeneratedAt(LocalDateTime.now())
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
    public List<UserResponse> getAllUsers() {
        if (SecurityUtil.isSuperAdmin()) {
            return userRepository.findAllByIsActiveTrue()
                    .stream()
                    .map(u -> mapToResponse(u, null))
                    .toList();
        }
        Long tenantId = SecurityUtil.getCurrentTenantId();
        return userRepository.findAllByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(u -> mapToResponse(u, null))
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
        user.setPinGeneratedAt(LocalDateTime.now());
        user.setIsPinResetRequired(true);
        userRepository.save(user);

        return PinResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .pin(rawPin)
                .message("PIN reset successfully. Share this PIN with the user.")
                .build();
    }

    @Override
    public BulkTenantUploadResponse bulkUpload(MultipartFile file) {
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();
        int rowNum = 1;

        Long tenantId = resolveTenantId(null);

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

                    Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                            .orElseThrow(() -> new FerosException(
                                    "Tenant not found", HttpStatus.NOT_FOUND));

                    String rawPin = generatePin();
                    String hashedPin = passwordEncoder.encode(rawPin);

                    User user = User.builder()
                            .tenant(tenant)
                            .name(name)
                            .phone(phone)
                            .pin(hashedPin)
                            .plainPin(rawPin)
                            .pinGeneratedAt(LocalDateTime.now())
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

    // Helper methods
    private String generatePin() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    private boolean isStaffRole(RoleName role) {
        return role == RoleName.DRIVER ||
                role == RoleName.CLEANER ||
                role == RoleName.SUPERVISOR;
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
                .isActive(true)
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

        staffProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
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
}