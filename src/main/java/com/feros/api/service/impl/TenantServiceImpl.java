package com.feros.api.service.impl;

import com.feros.api.dto.request.CreateTenantRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.TenantResponse;
import com.feros.api.entity.Tenant;
import com.feros.api.enums.SubscriptionStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.TenantRepository;
import com.feros.api.service.TenantService;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public TenantResponse createTenant(CreateTenantRequest request) {

        // Check duplicate email
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new FerosException("Email already exists", HttpStatus.CONFLICT);
        }

        // Check duplicate phone
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
                .trialStartDate(LocalDate.now())
                .trialEndDate(LocalDate.now().plusMonths(1))
                .isActive(true)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        return mapToResponse(saved);
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

        Tenant updated = tenantRepository.save(tenant);
        return mapToResponse(updated);
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

            // Skip header row
            csvReader.readNext();

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
                    String email = row[1].trim();
                    String phone = row[2].trim();
                    String address = row[3].trim();
                    String city = row[4].trim();
                    String state = row[5].trim();
                    String pincode = row[6].trim();
                    String gstin = row[7].trim();
                    String panNumber = row[8].trim();
                    String ownerName = row[9].trim();
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
                            .email(email)
                            .phone(phone)
                            .address(address)
                            .city(city)
                            .state(state)
                            .pincode(pincode)
                            .gstin(gstin)
                            .panNumber(panNumber)
                            .ownerName(ownerName)
                            .ownerPhone(ownerPhone)
                            .ownerEmail(ownerEmail)
                            .lorryCount(0)
                            .subscriptionStatus(SubscriptionStatus.TRIAL)
                            .trialStartDate(LocalDate.now())
                            .trialEndDate(LocalDate.now().plusMonths(1))
                            .isActive(true)
                            .build();

                    tenantRepository.save(tenant);
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
                .logoUrl(tenant.getLogoUrl())
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
}