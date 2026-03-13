package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private Long id;
    private Long tenantId;
    private String clientName;

    private Long clientTypeId;
    private String clientTypeName;

    private String phone;
    private String email;
    private String address;

    private Long cityId;
    private String cityName;
    private Long stateId;
    private String stateName;
    private String pincode;

    private String gstin;
    private String panNumber;

    private String contactPersonName;
    private String contactPersonPhone;
    private String contactPersonEmail;

    private Long paymentTermsId;
    private String paymentTermsName;
    private Integer creditDays;

    private BigDecimal creditLimit;
    private BigDecimal openingBalance;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}