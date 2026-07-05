package com.feros.api.dto.response;

import com.feros.api.enums.EquipmentBillingType;
import com.feros.api.enums.EquipmentInvoiceItemType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EquipmentInvoiceItemResponse {
    private Long id;
    private EquipmentInvoiceItemType itemType;
    private String description;
    private Long machineAssignmentId;
    private String serialNumber;       // denormalized for display
    private String equipmentTypeName;  // denormalized for display
    private EquipmentBillingType billingType;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal amount;
    private Integer sortOrder;
}
