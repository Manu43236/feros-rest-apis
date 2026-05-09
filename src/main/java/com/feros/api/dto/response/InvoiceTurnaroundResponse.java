package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceTurnaroundResponse {
    private String clientName;
    private int lrCount;
    private double avgTurnaroundDays;
    private long maxTurnaroundDays;
}
