package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkOrderDetailResponse {
    private WorkOrderResponse workOrder;
    private List<MachineAssignmentResponse> assignments;
    private List<DailyLogResponse> logs;
    private BillingSummaryResponse billing;
}
