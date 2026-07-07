package com.feros.api.dto.request;

import com.feros.api.enums.LeaseSessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LeaseSessionStartRequest {

    @NotNull(message = "Status is required")
    private LeaseSessionStatus status;

    // Start time — defaults to now on the backend if not provided
    private LocalDateTime startTime;

    // Driver — null means client's driver
    private Long driverStaffId;

    // Division — required when status is WORKING
    private Long divisionId;

    private String notes;
}
