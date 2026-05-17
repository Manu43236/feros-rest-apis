package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DocumentRequest {

    @NotNull(message = "Document type is required")
    private Long documentTypeId;

    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String fileUrl;
    private String issuerName;
    private String permitType;
    private String remarks;
}