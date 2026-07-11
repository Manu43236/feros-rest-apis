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
public class EquipmentDocumentResponse {
    private Long id;
    private Long equipmentId;
    private String serialNumber;
    private String registrationNumber;
    private Long documentTypeId;
    private String documentTypeName;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuerName;
    private String fileUrl;
    private Boolean isVerified;
    private BigDecimal cost;
    private LocalDate paidOn;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
