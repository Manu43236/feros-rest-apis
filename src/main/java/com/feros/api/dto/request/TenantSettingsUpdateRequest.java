package com.feros.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class TenantSettingsUpdateRequest {
    private String payCycle;
    private BigDecimal overtimeThresholdHours;
    private BigDecimal overtimeRateMultiplier;
    private BigDecimal maxAdvanceAmount;
    private BigDecimal maxAdvanceDeductionPerCycle;
    private Boolean isTripBonusEnabled;
    private BigDecimal tripBonusAmount;
    private Boolean attendanceEnforced;
    private LocalTime attendanceDeadlineTime;
    private Boolean requireTyreApproval;
    private Boolean requireSparePartApproval;
    private BigDecimal serviceGstRate;
    private Boolean serviceInvoiceGstEnabled;
    private String invoiceDescription;
}
