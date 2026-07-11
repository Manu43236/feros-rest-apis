package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EquipmentDocumentRequest {
    private Long documentTypeId;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuerName;
    private String fileUrl;
    private BigDecimal cost;
    private LocalDate paidOn;
    private String remarks;
    private Boolean isVerified;
}
