package com.feros.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartConsumptionByTypeResponse {
    private Long partId;
    private String partName;
    private String partNumber;
    private String category;
    private String unit;
    private int totalQuantity;
    private int serviceCount;
}
