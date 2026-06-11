package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdatePayrollRequest {

    private BigDecimal dailyRate;
    private BigDecimal monthlySalary;
    private BigDecimal overtimeHours;
    private BigDecimal tripBonus;
    /**
     * null  = keep existing deductions unchanged
     * empty = remove all deductions
     * items = replace all deductions with these
     */
    private List<DeductionItem> deductions;
    private String remarks;

    @Getter
    @Setter
    public static class DeductionItem {
        private Long deductionTypeId;
        private BigDecimal amount;
        private Long salaryAdvanceId;
        private String remarks;
    }
}
