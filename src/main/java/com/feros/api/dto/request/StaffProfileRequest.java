package com.feros.api.dto.request;

import com.feros.api.enums.SalaryType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class StaffProfileRequest {

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
    private String bankBranchName;
    private String aadharNumber;
    private String aadharName;
    private String nomineeName;
    private String nomineeRelation;
    private LocalDate nomineeDateOfBirth;
    private String nomineeAadharNumber;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String profilePhotoUrl;

    private SalaryType salaryType;
    private BigDecimal monthlySalary;

    private Boolean canAccessVehicles;
    private Boolean canAccessEquipment;
}