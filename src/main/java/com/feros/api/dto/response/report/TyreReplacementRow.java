package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreReplacementRow {
    private Long tyreId;
    private String serialNumber;
    private String brand;
    private String size;
    private String vehicleRegistration;
    private String positionCode;
    private LocalDate fittedDate;
    private double totalLifetimeKm;
    private double maxLifetimeKm;
    private double remainingKm;
    private double percentLifeUsed;
    private String urgency;   // HIGH / MEDIUM / LOW
}
