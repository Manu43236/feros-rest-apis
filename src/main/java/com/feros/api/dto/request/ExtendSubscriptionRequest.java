package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExtendSubscriptionRequest {
    @NotNull(message = "New end date is required")
    private LocalDate newEndDate;
    private String notes;
}
