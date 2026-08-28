package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class OperatorTodayResponse {

    private AssignmentSummary assignment;       // null if no active assignment today
    private List<EquipmentSessionLogResponse> todaySessions;
    private BigDecimal totalHmrToday;           // sum of hmrDelta for closed sessions

    @Getter
    @Builder
    public static class AssignmentSummary {
        private Long id;
        private Long workOrderId;
        private String workOrderNumber;
        private String clientName;
        private String siteName;
        private Long equipmentId;
        private String equipmentNumber;
        private String equipmentType;
        private BigDecimal lastKnownHmr;        // max(endHmr) from sessions — for start HMR pre-fill
    }
}
