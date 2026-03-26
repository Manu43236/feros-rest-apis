package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleServiceRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    @NotBlank(message = "Service type is required")
    private String serviceType;

    private String description;
    private BigDecimal cost;
    private BigDecimal odometerReading;
    private LocalDate nextServiceDueDate;
    private BigDecimal nextServiceDueOdometer;
    private String serviceCenterName;
    private String serviceCenterPhone;
    private String billUrl;
}
