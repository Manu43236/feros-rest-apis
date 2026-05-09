package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiresByVehicleResponse {
    private Long vehicleId;
    private String regNo;
    private String vehicleType;
    private int activeTireCount;
    private List<TireFittingItem> tires;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TireFittingItem {
        private Long fittingId;
        private Long tireId;
        private String serialNumber;
        private String brand;
        private String size;
        private String tireType;
        private String positionCode;
        private LocalDate fittedDate;
        private BigDecimal fittedAtKm;
        private BigDecimal kmDriven;
    }
}
