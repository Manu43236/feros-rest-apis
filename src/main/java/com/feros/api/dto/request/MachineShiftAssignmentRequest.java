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

    // Operator: staffId set = own staff; staffId null + clientOperator true = client's own;
    // staffId null + clientOperator false/absent = unassign the operator on this shift.
    private Long operatorStaffId;
    private String clientOperatorName;
    private boolean clientOperator;

    // Cleaner: same three states as operator.
    private Long cleanerStaffId;
    private String clientCleanerName;
    private boolean clientCleaner;
}
