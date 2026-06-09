package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GeneratePayrollRequest {

    @NotNull(message = "User is required")
    private Long userId;

    @NotNull(message = "Pay cycle start date is required")
    private LocalDate payCycleStartDate;

    @NotNull(message = "Pay cycle end date is required")
    private LocalDate payCycleEndDate;

    // Optional — if not provided, auto-fetched from staff profile's dailyRate or monthlySalary
    private BigDecimal dailyRate;
    private BigDecimal monthlySalary;

    private BigDecimal tripBonus;
    private BigDecimal overtimeHours;
    private List<DeductionItem> deductions;
    private String remarks;

    @Getter
    @Setter
    public static class DeductionItem {
        @NotNull private Long deductionTypeId;
        @NotNull private BigDecimal amount;
        private Long salaryAdvanceId;
        private String remarks;
    }
}