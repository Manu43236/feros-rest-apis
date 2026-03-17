package com.feros.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTenantRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private String prefix;
    private String address;
    private String city;
    private String state;
    private String pincode;

    private String gstin;
    private String panNumber;
    private String tanNumber;
    private String cinNumber;
    private String transportLicenseNumber;

    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
    private String accountHolderName;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Owner phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Owner phone must be 10 digits")
    private String ownerPhone;

    @Email(message = "Invalid owner email format")
    private String ownerEmail;
}