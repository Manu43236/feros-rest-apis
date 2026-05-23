package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TyresByVehicleResponse {
    private Long vehicleId;
    private String regNo;
    private String vehicleType;
    private int activeTyreCount;
    private List<TyreFittingItem> tyres;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TyreFittingItem {
        private Long fittingId;
        private Long tyreId;
        private String serialNumber;
        private String brand;
        private String size;
        private String tyreType;
        private String positionCode;
        private LocalDate fittedDate;
        private BigDecimal fittedAtKm;
        private BigDecimal kmDriven;
    }
}
