package com.feros.api.dto.request;

import com.feros.api.enums.TaxType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaxRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Rate is required")
    private BigDecimal rate;

    @NotNull(message = "Tax type is required")
    private TaxType taxType;
}