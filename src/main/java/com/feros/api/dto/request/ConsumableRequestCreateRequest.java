package com.feros.api.dto.request;

import lombok.Data;

@Data
public class ConsumableRequestCreateRequest {
    private Long vehicleId;
    private String notes;
}
