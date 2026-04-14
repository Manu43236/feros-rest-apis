package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TireRotationLogResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private LocalDate rotationDate;
    private BigDecimal odometerKm;
    private Long performedById;
    private String performedByName;
    private String notes;
    private List<TireRotationItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
