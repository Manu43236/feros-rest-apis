package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TireRotationRequest {
    private Long vehicleId;
    private LocalDate rotationDate;
    private BigDecimal odometerKm;
    private String notes;
    private List<TireRotationMoveRequest> moves;

    @Data
    public static class TireRotationMoveRequest {
        private Long tireId;
        private Long fromPositionId;
        private Long toPositionId;
    }
}
