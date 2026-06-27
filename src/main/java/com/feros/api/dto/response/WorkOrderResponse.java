package com.feros.api.dto.response;

import com.feros.api.enums.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WorkOrderResponse {
    private Long id;
    private Long tenantId;
    private String woNumber;
    private Long clientId;
    private String clientName;
    private String site;
    private RateType rateType;
    private BigDecimal rateAmount;
    private Integer shiftHours;
    private BigDecimal overtimeRatePerHour;
    private OperatorType operatorType;
    private Long operatorStaffId;
    private String operatorStaffName;
    private String hiredOperatorName;
    private String hiredOperatorPhone;
    private OperatorBilling operatorBilling;
    private BigDecimal operatorRatePerDay;
    private BigDecimal mobilizationCharge;
    private BigDecimal demobilizationCharge;
    private LocalDate startDate;
    private LocalDate endDate;
    private WorkOrderStatus status;
    private Long parentWoId;
    private String notes;
    private long machineCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
