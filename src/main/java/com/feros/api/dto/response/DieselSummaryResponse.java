package com.feros.api.dto.response;

import com.feros.api.enums.DieselBillingMode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DieselSummaryResponse {
    private Long assignmentId;
    private String machineName;
    private DieselBillingMode dieselBillingMode;
    private BigDecimal dieselRatePerLitre;
    private BigDecimal totalLitres;
    private BigDecimal billableAmount;   // totalLitres × rate, null unless BILLED_PER_LITRE
    private Boolean reimbursable;        // true if REIMBURSED_AT_ACTUALS
}
