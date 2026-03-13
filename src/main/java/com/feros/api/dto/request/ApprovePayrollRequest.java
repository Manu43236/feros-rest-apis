package com.feros.api.dto.request;

import com.feros.api.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ApprovePayrollRequest {
    @NotNull
    private PaymentMode paymentMode;
    private LocalDate paymentDate;
    private String referenceNumber;
    private String remarks;
}