package com.feros.api.dto.response;

import com.feros.api.enums.EquipmentOwnershipType;
import com.feros.api.enums.EquipmentWorkStatus;
import com.feros.api.enums.HireRateUnit;
import com.feros.api.enums.MeterType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EquipmentResponse {

    private Long id;

    // Catalog info (resolved)
    private Long equipmentTypeId;
    private String equipmentTypeName;
    private Long modelId;
    private String modelName;
    private Long makeId;
    private String makeName;

    private String serialNumber;
    private String registrationNumber;
    private Integer manufactureYear;

    private EquipmentOwnershipType ownershipType;

    // Finance
    private Boolean isFinanced;
    private String financerName;
    private LocalDate financeStartDate;
    private LocalDate financeEndDate;

    // Hire
    private String hiredFrom;
    private LocalDate hireStartDate;
    private LocalDate hireEndDate;
    private BigDecimal hireRate;
    private HireRateUnit hireRateUnit;

    private MeterType meterType;
    private BigDecimal currentMeterReading;
    private EquipmentWorkStatus workStatus;
    private Boolean isActive;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
