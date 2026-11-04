package com.feros.api.dto.response.report;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreCostPerKmRow {
    private Long tyreId;
    private String serialNumber;
    private String brand;
    private String size;
    private String tyreType;
    private String status;
    private double purchaseCost;
    private double totalRetreadingCost;
    private double totalRepairCost;
    private double effectiveCost;   // purchase + retread + repair
    private double billableKm;      // lifetime km we actually got out of it (minus km at purchase)
    private Double costPerKm;       // null when no billable km yet
}
