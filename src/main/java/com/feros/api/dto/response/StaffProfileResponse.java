package com.feros.api.dto.response;

import com.feros.api.enums.SalaryType;
import lombok.*;

import java.math.BigDecimal;
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
    private java.math.BigDecimal designationPayPerDay;
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
    private String bankBranchName;

    private String aadharNumber;
    private String aadharName;

    private String nomineeName;
    private String nomineeRelation;
    private LocalDate nomineeDateOfBirth;
    private String nomineeAadharNumber;

    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private Boolean licenseExpired;

    private String profilePhotoUrl;
    private List<DocumentResponse> documents;

    private SalaryType salaryType;
    private BigDecimal monthlySalary;

    private Boolean isActive;
    private Boolean canAccessVehicles;
    private Boolean canAccessEquipment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}