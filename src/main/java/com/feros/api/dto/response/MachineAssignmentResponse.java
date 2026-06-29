package com.feros.api.dto.response;

import com.feros.api.enums.AssignmentEndReason;
import com.feros.api.enums.OperatorType;
import lombok.Builder;
import lombok.Getter;

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
}
