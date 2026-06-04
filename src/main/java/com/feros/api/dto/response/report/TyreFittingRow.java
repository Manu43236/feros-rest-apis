package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreFittingRow {
    private Long fittingId;
    private LocalDate fittedDate;
    private String vehicleRegistration;
    private String vehicleType;
    private String tyreSerial;
    private String tyreBrand;
    private String tyreSize;
    private String tyreType;
    private String position;
    private double fittedAtKm;
    private String fittedBy;
}
