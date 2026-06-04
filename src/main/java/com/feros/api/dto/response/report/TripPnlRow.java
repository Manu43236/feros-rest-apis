package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPnlRow {
    private Long lrId;
    private String lrNumber;
    private LocalDate lrDate;
    private String registrationNumber;
    private String clientName;
    private String fromCity;
    private String toCity;
    private BigDecimal revenue;
    private BigDecimal tripExpenses;
    private BigDecimal netPnl;
}
