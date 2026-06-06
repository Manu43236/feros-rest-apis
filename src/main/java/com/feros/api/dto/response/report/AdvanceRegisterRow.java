package com.feros.api.dto.response.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdvanceRegisterRow {
    private Long advanceId;
    private String employeeName;
    private String role;
    private LocalDate advanceDate;
    private BigDecimal amount;
    private BigDecimal totalRepaid;
    private BigDecimal balanceAmount;
    private boolean fullyRepaid;
    private String reason;
    private String approvedBy;
}
