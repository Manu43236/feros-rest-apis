package com.feros.api.dto.response;

import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.RateType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class MachineAssignmentHistoryResponse {
    private Long id;
    private Long workOrderId;
    private String woNumber;
    private String clientName;
    private String site;
    private String workOrderStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private AssignmentEndReason endReason;
    private RateType rateType;
    private BigDecimal rateAmount;
    private BigDecimal totalHoursWorked;
}
