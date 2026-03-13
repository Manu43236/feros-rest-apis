package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryAdvanceResponse {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate advanceDate;
    private BigDecimal amount;
    private String reason;
    private BigDecimal totalRepaid;
    private BigDecimal balanceAmount;
    private Boolean isFullyRepaid;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}