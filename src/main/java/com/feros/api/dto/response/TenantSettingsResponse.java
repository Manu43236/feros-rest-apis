package com.feros.api.dto.response;

import com.feros.api.enums.PayCycle;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSettingsResponse {
    private Long id;
    private Long tenantId;
    private PayCycle payCycle;
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
    private java.math.BigDecimal serviceGstRate;
    private Boolean serviceInvoiceGstEnabled;
    private String invoiceDescription;
}
