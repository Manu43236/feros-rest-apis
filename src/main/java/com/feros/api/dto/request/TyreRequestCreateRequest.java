package com.feros.api.dto.request;

import lombok.Data;

@Data
public class TyreRequestCreateRequest {
    private Long vehicleId;
    private Long positionId;
    private String notes;
}
