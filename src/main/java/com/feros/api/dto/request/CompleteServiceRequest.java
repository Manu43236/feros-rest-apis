package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteServiceRequest {
    @NotNull(message = "Completed date is required")
    private LocalDate completedDate;
    private Integer odometer;
    private BigDecimal labourCharges;   // INTERNAL only — optional, defaults to 0
    private BigDecimal vendorAmount;    // EXTERNAL only — optional, can be updated later
    private String vendorInvoiceNo;     // EXTERNAL only — optional
    private BigDecimal completedCost;   // total bill from 3rd party, stored as record
}
