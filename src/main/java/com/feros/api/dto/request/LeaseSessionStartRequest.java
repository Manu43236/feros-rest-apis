package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LeaseSessionStartRequest {

    // Start time — defaults to now on the backend if not provided
    private LocalDateTime startTime;

    // Driver — null means client's driver
    private Long driverStaffId;

    // Division — required when client has divisions, optional otherwise
    private Long divisionId;

    private String notes;
}
