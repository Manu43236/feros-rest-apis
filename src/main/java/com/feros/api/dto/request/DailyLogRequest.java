package com.feros.api.dto.request;

import com.feros.api.enums.DailyLogStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DailyLogRequest {

    @NotNull(message = "Machine assignment is required")
    private Long machineAssignmentId;

    @NotNull(message = "Log date is required")
    private LocalDate logDate;

    @NotNull(message = "Status is required")
    private DailyLogStatus status;

    private BigDecimal fuelConsumed;
    private String notes;

    // Each division line: division + HMR range + notes
    private List<DailyLogDivisionRequest> divisions;
}
