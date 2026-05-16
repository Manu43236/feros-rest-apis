package com.feros.api.dto.request;

import lombok.Data;

@Data
public class TireRequestCreateRequest {
    private Long vehicleId;
    private Long positionId;
    private String notes;
}
