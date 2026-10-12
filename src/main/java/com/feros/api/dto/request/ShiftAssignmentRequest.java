package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Assign (or re-assign) a driver/cleaner to one shift on one day. Null staff id = client's own
 *  (tenant pays nothing). workDate is the shift's START date (night shift credited there). */
@Getter
@Setter
public class ShiftAssignmentRequest {

    @NotNull(message = "Shift is required")
    private Long shiftId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    // null = client's driver
    private Long driverStaffId;

    // null = client's cleaner
    private Long cleanerStaffId;
}
