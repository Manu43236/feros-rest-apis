package com.feros.api.dto.request;

import com.feros.api.enums.ServiceTriggeredBy;
import com.feros.api.enums.VehicleServiceType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleServiceRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Triggered by is required")
    private ServiceTriggeredBy triggeredBy;

    private Long breakdownId;

    @NotNull(message = "Service type is required")
    private VehicleServiceType serviceType;

    private String vendorName;
    private String location;
    private Integer dueAtOdometer;
    private LocalDate serviceDate;
    private Integer odometer;
    private String notes;

    @NotEmpty(message = "At least one task is required")
    private List<VehicleServiceTaskRequest> tasks;
}
