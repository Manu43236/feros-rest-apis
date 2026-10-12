package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

/** One shift window on a shift-duty lease. Windows must not overlap; start > end crosses midnight. */
@Getter
@Setter
public class ShiftDefinitionRequest {

    @NotNull(message = "Shift number is required")
    private Integer shiftNumber;

    @NotNull(message = "Shift start time is required")
    private LocalTime startTime;

    @NotNull(message = "Shift end time is required")
    private LocalTime endTime;

    @NotNull(message = "Driver pay is required")
    private BigDecimal driverPay;

    // optional — null when no cleaner on this shift
    private BigDecimal cleanerPay;
}
