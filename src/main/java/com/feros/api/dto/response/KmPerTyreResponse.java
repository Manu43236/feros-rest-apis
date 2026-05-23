package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KmPerTyreResponse {
    private Long fittingId;
    private Long tyreId;
    private String serialNumber;
    private String brand;
    private String size;
    private String tyreType;
    private Long vehicleId;
    private String vehicleRegNo;
    private String positionCode;
    private LocalDate fittedDate;
    private BigDecimal fittedAtKm;
    private BigDecimal removedAtKm;
    private BigDecimal kmDriven;
    private boolean active;
}
