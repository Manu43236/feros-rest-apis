package com.feros.api.dto.response;

import com.feros.api.enums.OperatorType;
import com.feros.api.enums.WorkEntryStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WorkEntryResponse {
    private Long id;
    private Long machineAssignmentId;
    private WorkEntryStatus status;

    private OperatorType operatorType;
    private Long operatorStaffId;
    private String operatorStaffName;
    private String hiredOperatorName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal startMeter;
    private BigDecimal endMeter;
    private BigDecimal hoursWorked;
    private String notes;
}
