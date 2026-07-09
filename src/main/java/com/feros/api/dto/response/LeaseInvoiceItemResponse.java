package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class LeaseInvoiceItemResponse {
    private Long id;
    private Long leaseVehicleAssignmentId;
    private String registrationNumber;
    private String description;
    private Integer days;
    private BigDecimal rate;
    private BigDecimal amount;
    private Integer sortOrder;
}
