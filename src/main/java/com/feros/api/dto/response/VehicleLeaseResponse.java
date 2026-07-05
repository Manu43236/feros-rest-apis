package com.feros.api.dto.response;

import com.feros.api.enums.LeaseStatus;
import com.feros.api.enums.RateType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class VehicleLeaseResponse {
    private Long id;
    private Long tenantId;
    private String leaseNumber;
    private Long clientId;
    private String clientName;
    private String site;
    private LocalDate startDate;
    private LocalDate endDate;
    private RateType rateType;
    private LeaseStatus status;
    private String notes;
    private long vehicleCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
