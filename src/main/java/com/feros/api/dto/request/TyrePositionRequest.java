package com.feros.api.dto.request;

import com.feros.api.enums.TyrePositionType;
import lombok.Data;

@Data
public class TyrePositionRequest {
    private Long vehicleId;
    private String positionCode;
    private TyrePositionType positionType;
    private Integer displayOrder;
}
