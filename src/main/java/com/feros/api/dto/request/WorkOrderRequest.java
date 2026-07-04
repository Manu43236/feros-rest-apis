package com.feros.api.dto.request;

import com.feros.api.enums.OperatorBilling;
import com.feros.api.enums.OperatorType;
import com.feros.api.enums.RateType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class WorkOrderRequest {

    @NotNull(message = "Client is required")
    private Long clientId;

    private String site;

    private RateType rateType;
    private BigDecimal rateAmount;

    private Integer shiftHours;             // DAILY_SHIFT only, defaults to 8
    private BigDecimal overtimeRatePerHour; // DAILY_SHIFT only, optional

    private OperatorType operatorType;
    private Long operatorStaffId;           // OWN_STAFF only
    private String hiredOperatorName;       // HIRED only
    private String hiredOperatorPhone;      // HIRED only
    private OperatorBilling operatorBilling;
    private BigDecimal operatorRatePerDay;  // BILLED_SEPARATELY only

    private BigDecimal mobilizationCharge;
    private BigDecimal demobilizationCharge;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    private LocalDate endDate;

    private Long parentWoId;
    private String notes;
}
