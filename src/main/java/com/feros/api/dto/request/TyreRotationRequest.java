package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TyreRotationRequest {
    private Long vehicleId;
    private LocalDate rotationDate;
    private BigDecimal odometerKm;
    private String notes;
    private List<TyreRotationMoveRequest> moves;

    @Data
    public static class TyreRotationMoveRequest {
        private Long tyreId;
        private Long fromPositionId;
        private Long toPositionId;
    }
}
