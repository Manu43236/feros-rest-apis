package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ClientStatementResponse {
    private Long clientId;
    private String clientName;
    private String clientPhone;
    private String gstin;
    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal closingBalance;
    private List<ClientStatementRow> rows;
}
