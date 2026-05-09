package com.feros.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePartConsumptionResponse {
    private Long vehicleId;
    private String regNo;
    private String vehicleType;
    private String partName;
    private String partNumber;
    private String category;
    private int totalQuantity;
}
