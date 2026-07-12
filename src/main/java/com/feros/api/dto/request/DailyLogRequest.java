package com.feros.api.dto.request;

import com.feros.api.enums.DailyLogStatus;
import com.feros.api.enums.IdleAttribution;
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

    // E4 — hour breakdown
    private BigDecimal workingHours;
    private BigDecimal idleHours;
    private BigDecimal standbyHours;
    private BigDecimal breakdownHours;

    // E4 — idle attribution
    private IdleAttribution idleAttribution;
    private String idleReason;

    // E4 — client-signed slip photo
    private String signedSlipPhotoUrl;
}
