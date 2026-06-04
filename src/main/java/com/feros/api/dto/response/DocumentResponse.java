package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
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
    private BigDecimal cost;
    private LocalDate paidOn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}