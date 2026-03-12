package com.feros.api.dto.response;

import com.feros.api.enums.RoleName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String phone;
    private RoleName role;
    private Long tenantId;
    private String companyName;
    private Boolean isActive;
    private Boolean isPinResetRequired;
    private String generatedPin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Staff Profile fields
    private String employmentType;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String profilePhotoUrl;
}