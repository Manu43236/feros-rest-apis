package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuspendSubscriptionRequest {
    @NotBlank(message = "Reason is required")
    private String notes;
}
