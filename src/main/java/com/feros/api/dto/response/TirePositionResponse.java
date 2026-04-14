package com.feros.api.dto.response;

import com.feros.api.enums.TirePositionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TirePositionResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String positionCode;
    private TirePositionType positionType;
    private Integer displayOrder;
    // current tire fitted here (null if empty)
    private TireFittingResponse currentFitting;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
