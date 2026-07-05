package com.feros.api.dto.request;

import com.feros.api.enums.EquipmentServiceType;
import com.feros.api.enums.ServicePayerType;
import com.feros.api.enums.ServiceTriggeredBy;
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
public class EquipmentServiceRequest {

    @NotNull(message = "Triggered by is required")
    private ServiceTriggeredBy triggeredBy;

    @NotNull(message = "Service type is required")
    private EquipmentServiceType serviceType;

    private ServicePayerType payerType;
    private String vendorName;
    private String location;
    private LocalDate serviceDate;
    private BigDecimal hmrAtService;
    private BigDecimal dueAtHmr;
    private String notes;

    // Insurance fields
    private String insuranceClaimNo;
    private BigDecimal insuranceClaimAmt;

    // Compliance fields
    private String certificateNumber;
    private LocalDate certificateValidUntil;

    private Boolean isEscalated;

    private List<EquipmentServiceTaskRequest> tasks;
}
