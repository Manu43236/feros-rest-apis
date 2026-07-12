package com.feros.api.dto.request;

import com.feros.api.enums.AmendmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class WoAmendmentRequest {

    @NotNull(message = "Amendment type is required")
    private AmendmentType amendmentType;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private String oldValue;
    private String newValue;
    private String reason;
}
