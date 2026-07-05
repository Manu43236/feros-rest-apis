package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class LeaseVehicleAssignmentRequest {

    @NotNull(message = "Vehicle is required")
    private Long vehicleId;

    // Null when client provides their own driver
    private Long driverStaffId;

    @NotNull(message = "Rate per vehicle is required")
    private BigDecimal ratePerVehicle;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal odometerAtStart;

    private String notes;
}
