package com.feros.api.dto.request;

import com.feros.api.enums.TirePositionType;
import lombok.Data;

@Data
public class TirePositionRequest {
    private Long vehicleId;
    private String positionCode;
    private TirePositionType positionType;
    private Integer displayOrder;
}
