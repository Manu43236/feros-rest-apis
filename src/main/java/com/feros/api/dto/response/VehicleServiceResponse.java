package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDate serviceDate;
    private String serviceType;
    private String description;
    private BigDecimal cost;
    private BigDecimal odometerReading;
    private LocalDate nextServiceDueDate;
    private BigDecimal nextServiceDueOdometer;
    private String serviceCenterName;
    private String serviceCenterPhone;
    private String billUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
