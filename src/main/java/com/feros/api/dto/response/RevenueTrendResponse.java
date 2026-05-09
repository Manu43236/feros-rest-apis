package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendResponse {
    private String period;   // "Jan 2026"
    private int year;
    private int month;
    private int invoiceCount;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalRevenue;
}
