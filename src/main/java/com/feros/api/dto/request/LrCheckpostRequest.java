package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class LrCheckpostRequest {

    @NotBlank(message = "Checkpost name is required")
    private String checkpostName;

    private String location;
    private BigDecimal fineAmount;
    private String fineReceiptNumber;
    private LocalDateTime finePaidAt;
    private String remarks;
}