package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ClientRequest {

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotNull(message = "Client type is required")
    private Long clientTypeId;

    private String phone;
    private String email;
    private String address;
    private Long cityId;
    private Long stateId;
    private String pincode;
    private String gstin;
    private String panNumber;
    private String contactPersonName;
    private String contactPersonPhone;
    private String contactPersonEmail;
    private Long paymentTermsId;
    private BigDecimal creditLimit;
    private BigDecimal openingBalance;
}