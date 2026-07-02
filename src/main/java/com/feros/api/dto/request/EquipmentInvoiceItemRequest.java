package com.feros.api.dto.request;

import com.feros.api.enums.EquipmentBillingType;
import com.feros.api.enums.EquipmentInvoiceItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EquipmentInvoiceItemRequest {

    @NotNull(message = "Item type is required")
    private EquipmentInvoiceItemType itemType;

    @NotBlank(message = "Description is required")
    private String description;

    // MACHINE items only
    private Long machineAssignmentId;
    private EquipmentBillingType billingType;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotNull(message = "Rate is required")
    private BigDecimal rate;

    private Integer sortOrder;
}
