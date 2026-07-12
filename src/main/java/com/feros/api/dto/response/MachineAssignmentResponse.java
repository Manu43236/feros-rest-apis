package com.feros.api.dto.response;

import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.AttachmentType;
import com.feros.api.enums.HireType;
import com.feros.api.enums.HireRateUnit;
import com.feros.api.enums.OperatorType;
import com.feros.api.enums.ProviderSide;
import com.feros.api.enums.RateType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class MachineAssignmentResponse {
    private Long id;
    private Long workOrderId;
    private Long equipmentId;
    private String serialNumber;
    private String equipmentTypeName;
    private String makeName;
    private String modelName;
    private LocalDate startDate;
    private LocalDate endDate;
    private AssignmentEndReason endReason;
    private Boolean isActive;
    private OperatorType operatorType;
    private Long operatorStaffId;
    private String operatorStaffName;
    private String hiredOperatorName;
    private String hiredOperatorPhone;
    private WorkEntryResponse activeWorkEntry;
    private Long divisionId;
    private String divisionName;
    private RateType rateType;
    private BigDecimal rateAmount;
    // Attachment (KAN-13)
    private Long attachmentId;
    private String attachmentName;
    private AttachmentType attachmentType;
    private BigDecimal attachmentRate;
    private HireRateUnit attachmentRateUnit;
    // KAN-17 per-machine billing terms
    private HireType hireType;
    private BigDecimal guaranteedHours;
    private BigDecimal overtimeRate;
    private ProviderSide dieselByWhom;
    // KAN-18 on/off-hire dates
    private LocalDate onHireDate;
    private LocalDate offHireDate;
    // KAN-20 swap back-link
    private Long swappedFromAssignmentId;
}
