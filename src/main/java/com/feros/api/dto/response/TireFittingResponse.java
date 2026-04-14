package com.feros.api.dto.response;

import com.feros.api.enums.TireRemovalReason;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TireFittingResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long tireId;
    private String tireSerialNumber;
    private String tireBrand;
    private String tireSize;
    private Long positionId;
    private String positionCode;
    private BigDecimal fittedAtKm;
    private LocalDate fittedDate;
    private Long fittedById;
    private String fittedByName;
    private BigDecimal removedAtKm;
    private LocalDate removedDate;
    private TireRemovalReason removalReason;
    private Long removedById;
    private String removedByName;
    private Long rotationLogId;
    private BigDecimal kmDriven;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
