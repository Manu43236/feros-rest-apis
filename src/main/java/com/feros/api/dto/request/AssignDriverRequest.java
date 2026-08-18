package com.feros.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssignDriverRequest {
    private Long driverStaffId; // null = remove driver (client's driver)
}
