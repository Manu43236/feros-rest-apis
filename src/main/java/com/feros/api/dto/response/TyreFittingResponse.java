package com.feros.api.dto.response;

import com.feros.api.enums.TyreRemovalReason;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TyreFittingResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long tyreId;
    private String tyreSerialNumber;
    private String tyreBrand;
    private String tyreSize;
    private BigDecimal tyreMaxLifetimeKm;
    private BigDecimal tyreTotalLifetimeKm;
    private Long positionId;
    private String positionCode;
    private BigDecimal fittedAtKm;
    private LocalDate fittedDate;
    private Long fittedById;
    private String fittedByName;
    private BigDecimal removedAtKm;
    private LocalDate removedDate;
    private TyreRemovalReason removalReason;
    private Long removedById;
    private String removedByName;
    private Long rotationLogId;
    private BigDecimal kmDriven;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
