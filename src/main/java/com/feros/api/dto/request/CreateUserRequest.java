package com.feros.api.dto.request;

import com.feros.api.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotNull(message = "Role is required")
    private RoleName role;

    // Required only when SUPER_ADMIN creates user
    private Long tenantId;

    // Staff profile fields
    // Required for DRIVER, CLEANER, SUPERVISOR
    private Long employmentTypeId;
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

    // Required for DRIVER only
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private Long designationId;

    // Equipment module access
    private Boolean canAccessEquipment;

}