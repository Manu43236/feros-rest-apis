package com.feros.api.dto.request;

import com.feros.api.enums.ServicePayerType;
import com.feros.api.enums.ServiceTriggeredBy;
import com.feros.api.enums.VehicleServiceType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
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

    private ServicePayerType payerType;
    private String vendorName;
    private String location;
    private Integer dueAtOdometer;
    private LocalDate serviceDate;
    private Integer odometer;
    private String notes;

    // Insurance fields (required when payerType = INSURANCE)
    private String insuranceClaimNo;
    private BigDecimal insuranceClaimAmt;

    // Compliance fields (required when triggeredBy = COMPLIANCE)
    private String certificateNumber;
    private LocalDate certificateValidUntil;

    // Escalation flag (internal mechanic escalated to 3rd party)
    private Boolean isEscalated;

    private List<VehicleServiceTaskRequest> tasks;
}
