package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DriverAssignmentHistoryResponse {
    private Long allocationId;
    private String driverName;
    private String driverPhone;
    private String roleName;
    private String registrationNumber;
    private String orderNumber;
    private String clientName;
    private String fromCity;
    private String toCity;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;
    private String allocationStatus;
}
