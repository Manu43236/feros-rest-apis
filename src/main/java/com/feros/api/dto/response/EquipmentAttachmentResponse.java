package com.feros.api.dto.response;

import com.feros.api.enums.AttachmentType;
import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.HireRateUnit;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentAttachmentResponse {
    private Long id;
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
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
