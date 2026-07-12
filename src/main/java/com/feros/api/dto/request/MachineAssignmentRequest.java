package com.feros.api.dto.request;

import com.feros.api.enums.DieselBillingMode;
import com.feros.api.enums.HireType;
import com.feros.api.enums.RateType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MachineAssignmentRequest {

    @NotNull(message = "Equipment is required")
    private Long equipmentId;

    private LocalDate startDate; // defaults to today if null

    // Per-machine rate — overrides WO rate for invoicing
    private RateType rateType;
    private BigDecimal rateAmount;

    // KAN-17 per-machine billing terms
    private HireType hireType;
    private BigDecimal guaranteedHours;
    private BigDecimal overtimeRate;
    private DieselBillingMode dieselBillingMode;
    private BigDecimal dieselRatePerLitre;

    // KAN-18 actual on/off-hire dates
    private LocalDate onHireDate;
    private LocalDate offHireDate;
}
