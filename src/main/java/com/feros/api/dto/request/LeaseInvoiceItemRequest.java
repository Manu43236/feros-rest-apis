package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class LeaseInvoiceItemRequest {
    private Long leaseVehicleAssignmentId;
    private String registrationNumber;
    private String description;
    private Integer days;
    private BigDecimal rate;
    private BigDecimal amount; // pre-computed by frontend
    private Integer sortOrder;
}
