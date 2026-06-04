package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreRemovalRow {
    private Long fittingId;
    private LocalDate removedDate;
    private String vehicleRegistration;
    private String vehicleType;
    private String tyreSerial;
    private String tyreBrand;
    private String tyreSize;
    private String position;
    private double fittedAtKm;
    private double removedAtKm;
    private double kmDriven;
    private String removalReason; // ROTATION / WORN / PUNCTURE / DAMAGE / RETREAD / SCRAP / OTHER
    private String removedBy;
}
