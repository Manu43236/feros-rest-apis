package com.feros.api.dto.request;

import com.feros.api.enums.PayCycle;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
public class TenantSettingsRequest {
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
}