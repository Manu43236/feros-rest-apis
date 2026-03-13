package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffProfileResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String roleName;
    private Long tenantId;

    private Long designationId;
    private String designationName;
    private Long employmentTypeId;
    private String employmentTypeName;

    private LocalDate dateOfBirth;
    private LocalDate joiningDate;

    private String address;
    private Long cityId;
    private String cityName;
    private Long stateId;
    private String stateName;
    private String pincode;

    private String emergencyContactName;
    private String emergencyContactPhone;

    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;

    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private Boolean licenseExpired;

    private String profilePhotoUrl;
    private List<DocumentResponse> documents;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}