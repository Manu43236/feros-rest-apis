package com.feros.api.dto.request;

import com.feros.api.enums.RateType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MachineAssignmentRequest {

    @NotNull(message = "Equipment is required")
    private Long equipmentId;

    private LocalDate startDate; // defaults to today if null

    // Optional per-machine rate — overrides WO rate for invoicing
    private RateType rateType;
    private BigDecimal rateAmount;
}
