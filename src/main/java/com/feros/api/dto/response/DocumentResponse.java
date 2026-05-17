package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private Long id;
    private Long documentTypeId;
    private String documentTypeName;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean isExpired;
    private String fileUrl;
    private String issuerName;
    private String permitType;
    private Boolean isVerified;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}