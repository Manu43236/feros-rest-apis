package com.feros.api.dto.response;

import com.feros.api.enums.VehicleAllocationStatus;
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
public class VehicleAllocationResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String vehicleTypeName;
    private BigDecimal allocatedWeight;
    private LocalDate expectedLoadDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualLoadDate;
    private LocalDate actualDeliveryDate;
    private VehicleAllocationStatus allocationStatus;
    private String remarks;
    private Long allocatedById;
    private String allocatedByName;
    private Long currentDriverId;
    private String currentDriverName;
    private String currentDriverPhone;
    private Long currentCleanerId;
    private String currentCleanerName;
    private String currentCleanerPhone;
    private List<StaffAllocationResponse> staffAllocations;
    private List<StaffAllocationResponse> staffHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}