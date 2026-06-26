package com.feros.api.dto.request;

import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.EquipmentWorkStatus;
import com.feros.api.enums.HireRateUnit;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EquipmentRequest {

    @NotNull
    private Long equipmentTypeId;

    private String serialNumber;
    private String registrationNumber;
    private Integer manufactureYear;

    @NotNull
    private EquipmentOwnershipType ownershipType;

    // Finance (OWNED)
    private Boolean isFinanced;
    private String financerName;
    private LocalDate financeStartDate;
    private LocalDate financeEndDate;

    // Hire (HIRED_IN)
    private String hiredFrom;
    private LocalDate hireStartDate;
    private LocalDate hireEndDate;
    private BigDecimal hireRate;
    private HireRateUnit hireRateUnit;

    private BigDecimal currentMeterReading;
    private EquipmentWorkStatus workStatus;
    private Boolean isActive;
    private String notes;
}
