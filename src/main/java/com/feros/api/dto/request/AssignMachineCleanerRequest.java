package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

/** Standing cleaner assignment on a machine (non-shift). Mirror of {@link AssignOperatorRequest}.
 *  cleanerStaffId set = our staff; clientCleanerName set = client's own (no payroll); both null = remove. */
@Getter
@Setter
public class AssignMachineCleanerRequest {
    private Long cleanerStaffId;       // our staff (user id)
    private String clientCleanerName;  // client's own cleaner (free-text, no payroll)
    private boolean swap;              // release the staff from their current machine first
}
