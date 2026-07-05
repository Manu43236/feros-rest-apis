package com.feros.api.dto.response;

import com.feros.api.enums.EquipmentServiceType;
import com.feros.api.enums.ServicePayerType;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTriggeredBy;
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
public class EquipmentServiceResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentIdentifier;
    private String serviceNumber;
    private ServiceTriggeredBy triggeredBy;
    private EquipmentServiceType serviceType;
    private ServicePayerType payerType;
    private ServiceStatus status;
    private String displayStatus;
    private BigDecimal hmrAtService;
    private BigDecimal dueAtHmr;
    private String vendorName;
    private String location;
    private LocalDate serviceDate;
    private LocalDate completedDate;
    private BigDecimal completedHmr;
    private LocalDateTime startedAt;
    private BigDecimal totalCost;
    private String insuranceClaimNo;
    private BigDecimal insuranceClaimAmt;
    private String certificateNumber;
    private LocalDate certificateValidUntil;
    private Boolean isEscalated;
    private String notes;
    private Long invoiceId;
    private List<EquipmentServiceTaskResponse> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
