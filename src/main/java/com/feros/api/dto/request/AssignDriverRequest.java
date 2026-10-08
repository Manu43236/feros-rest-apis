package com.feros.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssignDriverRequest {
    private Long driverStaffId;      // null = client's driver
    private String clientDriverName; // optional name when client provides driver
    private boolean swap = false;    // true = unassign the driver from their current normal/order vehicle first
}
