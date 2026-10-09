package com.feros.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** Mirror of {@link AssignDriverRequest} for lease cleaners. */
@Getter
@NoArgsConstructor
public class AssignCleanerRequest {
    private Long cleanerStaffId;      // null = client's cleaner
    private String clientCleanerName; // optional name when client provides cleaner
    private boolean swap = false;     // true = unassign the cleaner from their current normal/order vehicle first
}
