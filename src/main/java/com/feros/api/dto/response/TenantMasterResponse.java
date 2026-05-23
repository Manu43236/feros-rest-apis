package com.feros.api.dto.response;

import com.feros.api.enums.PayCycle;
import com.feros.api.enums.RoleName;
import com.feros.api.enums.VehicleStatusType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantMasterResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String description;

    // Route fields
    private Long sourceCityId;
    private String sourceCityName;
    private Long destinationCityId;
    private String destinationCityName;
    private BigDecimal distanceInKm;
    private Integer estimatedHours;

    // VehicleStatus fields
    private VehicleStatusType statusType;

    // Designation fields
    private RoleName roleType;

    // PayRate fields
    private Long designationId;
    private String designationName;
    private Long vehicleTypeId;
    private String vehicleTypeName;
    private BigDecimal payPerDay;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    // PaymentTerms fields
    private Integer creditDays;

    // TenantSettings fields
    private PayCycle payCycle;
    private BigDecimal overtimeThresholdHours;
    private BigDecimal overtimeRateMultiplier;
    private BigDecimal maxAdvanceAmount;
    private BigDecimal maxAdvanceDeductionPerCycle;
    private Boolean isTripBonusEnabled;
    private BigDecimal tripBonusAmount;
    private Boolean attendanceEnforced;
    private LocalTime attendanceDeadlineTime;
    private Boolean requireTyreApproval;
    private Boolean requireSparePartApproval;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}