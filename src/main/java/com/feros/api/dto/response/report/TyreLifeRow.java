package com.feros.api.dto.response.report;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreLifeRow {
    private Long tyreId;
    private String serialNumber;
    private String brand;
    private String size;
    private String tyreType;
    private double totalLifetimeKm;
    private double maxLifetimeKm;
    private double percentLifeUsed;
    private int retreadCount;
    private String status;
}
