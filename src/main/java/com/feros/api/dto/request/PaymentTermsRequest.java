package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentTermsRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private Integer creditDays = 0;
}