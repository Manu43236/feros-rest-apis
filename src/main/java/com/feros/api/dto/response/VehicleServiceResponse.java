package com.feros.api.dto.response;

import com.feros.api.enums.ServicePayerType;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import com.feros.api.enums.VehicleServiceType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleServiceResponse {
    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String serviceNumber;
    private ServiceTriggeredBy triggeredBy;
    private Long breakdownId;
    private VehicleServiceType serviceType;
    private ServicePayerType payerType;
    private String vendorName;
    private String location;
    private ServiceStatus status;
    private String displayStatus; // OPEN, DUE_SOON, OVERDUE, COMPLETED
    private Integer dueAtOdometer;
    private LocalDate serviceDate;
    private LocalDate completedDate;
    private Integer odometer;
    private String notes;
    private BigDecimal totalCost;
    private String insuranceClaimNo;
    private BigDecimal insuranceClaimAmt;
    private String certificateNumber;
    private LocalDate certificateValidUntil;
    private Boolean isEscalated;
    private List<VehicleServiceTaskResponse> tasks;
    private LocalDateTime startedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
