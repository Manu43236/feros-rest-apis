package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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

    // Odometer — optional, pre-filled on frontend from last session's odometerEnd
    private BigDecimal odometerStart;

    private String notes;
}
