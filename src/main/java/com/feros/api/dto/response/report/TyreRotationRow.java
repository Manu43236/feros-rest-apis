package com.feros.api.dto.response.report;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TyreRotationRow {
    private Long rotationLogId;
    private LocalDate rotationDate;
    private String vehicleRegistration;
    private String vehicleType;
    private double odometerKm;
    private String performedBy;
    private int tyresRotated;
    private List<TyreMovement> movements;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TyreMovement {
        private String tyreSerial;
        private String tyreBrand;
        private String fromPosition;
        private String toPosition;
    }
}
