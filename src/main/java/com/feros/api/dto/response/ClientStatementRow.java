package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ClientStatementRow {
    private String type;          // "INVOICE" or "PAYMENT"
    private LocalDate date;
    private String referenceNumber; // invoice number or payment reference
    private String description;
    private BigDecimal debit;     // invoice amount (amount client owes)
    private BigDecimal credit;    // payment amount (amount client paid)
    private BigDecimal balance;   // running balance
}
