package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KmPerTireResponse {
    private Long fittingId;
    private Long tireId;
    private String serialNumber;
    private String brand;
    private String size;
    private String tireType;
    private Long vehicleId;
    private String vehicleRegNo;
    private String positionCode;
    private LocalDate fittedDate;
    private BigDecimal fittedAtKm;
    private BigDecimal removedAtKm;
    private BigDecimal kmDriven;
    private boolean active;
}
