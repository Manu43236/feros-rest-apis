package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

/** One shift window on a shift-duty work order. Windows must not overlap; start > end crosses midnight.
 *  Equipment mirror of {@link ShiftDefinitionRequest} (operator/cleaner instead of driver/cleaner). */
@Getter
@Setter
public class WorkOrderShiftDefinitionRequest {

    @NotNull(message = "Shift number is required")
    private Integer shiftNumber;

    @NotNull(message = "Shift start time is required")
    private LocalTime startTime;

    @NotNull(message = "Shift end time is required")
    private LocalTime endTime;

    @NotNull(message = "Operator pay is required")
    private BigDecimal operatorPay;

    // optional — null when no cleaner on this shift
    private BigDecimal cleanerPay;
}
