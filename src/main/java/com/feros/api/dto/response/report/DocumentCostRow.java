package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCostRow {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private String documentTypeName;
    private String documentNumber;
    private String issuerName;
    private LocalDate paidOn;
    private BigDecimal cost;
}
