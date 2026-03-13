package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollDeductionResponse {
    private Long id;
    private Long deductionTypeId;
    private String deductionTypeName;
    private BigDecimal amount;
    private Long salaryAdvanceId;
    private String remarks;
}