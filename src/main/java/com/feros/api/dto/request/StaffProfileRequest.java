package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StaffProfileRequest {

    @NotNull(message = "Employment type is required")
    private Long employmentTypeId;

    private Long designationId;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
    private String address;
    private Long cityId;
    private Long stateId;
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