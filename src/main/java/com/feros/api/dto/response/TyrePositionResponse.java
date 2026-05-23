package com.feros.api.dto.response;

import com.feros.api.enums.TyrePositionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TyrePositionResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String positionCode;
    private TyrePositionType positionType;
    private Integer displayOrder;
    // current tyre fitted here (null if empty)
    private TyreFittingResponse currentFitting;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
