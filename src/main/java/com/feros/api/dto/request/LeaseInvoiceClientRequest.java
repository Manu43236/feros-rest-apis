package com.feros.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Client-level lease invoice creation — one invoice spanning vehicle lines from
 * multiple leases of the same client. Each item carries its own
 * leaseVehicleAssignmentId so every line still traces back to its lease/vehicle.
 */
@Getter @Setter
public class LeaseInvoiceClientRequest {

    @NotNull(message = "Client is required")
    private Long clientId;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @NotNull(message = "Billing period start is required")
    private LocalDate billingPeriodStart;

    @NotNull(message = "Billing period end is required")
    private LocalDate billingPeriodEnd;

    // Intra-state: cgst + sgst | Inter-state: igst
    private BigDecimal cgstPercentage;
    private BigDecimal sgstPercentage;
    private BigDecimal igstPercentage;

    private String notes;

    @Valid
    @NotNull(message = "At least one line item is required")
    private List<LeaseInvoiceItemRequest> items;
}
