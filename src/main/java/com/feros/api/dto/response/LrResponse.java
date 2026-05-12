package com.feros.api.dto.response;

import com.feros.api.enums.LrStatus;
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
public class LrResponse {
    private Long id;
    private Long tenantId;
    private String lrNumber;

    private Long orderId;
    private String orderNumber;

    private Long vehicleAllocationId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String vehicleTypeName;

    private Long clientId;
    private String clientName;

    private String fromCity;
    private String toCity;

    private LocalDate lrDate;
    private BigDecimal vehicleCapacity;
    private BigDecimal allocatedWeight;
    private BigDecimal loadedWeight;
    private BigDecimal overloadWeight;
    private BigDecimal deliveredWeight;
    private BigDecimal weightVariance;
    private Boolean isOverloaded;

    private LocalDateTime loadedAt;
    private LocalDateTime deliveredAt;

    private LrStatus lrStatus;
    private String remarks;

    private List<LrCheckpostResponse> checkposts;
    private List<LrChargeResponse> charges;

    private Long createdById;
    private String createdByName;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}