package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateDocumentRequest {
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String fileUrl;
    private String issuerName;
    private String permitType;
    private String remarks;
    private BigDecimal cost;
    private LocalDate paidOn;
}
