package com.feros.api.dto.request;

import com.feros.api.enums.AttachmentType;
import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.HireRateUnit;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EquipmentAttachmentRequest {
    private String name;
    private AttachmentType type;
    private String serialNumber;
    private EquipmentOwnershipType ownershipType;
    private String hiredFrom;
    private LocalDate hireStartDate;
    private LocalDate hireEndDate;
    private BigDecimal defaultRate;
    private HireRateUnit rateUnit;
    private String notes;
}
