package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BulkGeneratePayrollRequest {

    @NotNull(message = "Pay cycle start date is required")
    private LocalDate payCycleStartDate;

    @NotNull(message = "Pay cycle end date is required")
    private LocalDate payCycleEndDate;

    @NotNull(message = "User IDs are required")
    private List<Long> userIds;
}
