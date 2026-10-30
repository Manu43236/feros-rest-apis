package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Assign (or re-assign) an operator/cleaner to one machine shift on one day. Null staff id = client's
 *  own (tenant pays nothing). workDate is the shift's START date (night shift credited there).
 *  Equipment mirror of {@link ShiftAssignmentRequest}. */
@Getter
@Setter
public class MachineShiftAssignmentRequest {

    @NotNull(message = "Shift is required")
    private Long shiftId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    // null = client's operator
    private Long operatorStaffId;
    private String clientOperatorName;

    // null = client's cleaner
    private Long cleanerStaffId;
    private String clientCleanerName;
}
