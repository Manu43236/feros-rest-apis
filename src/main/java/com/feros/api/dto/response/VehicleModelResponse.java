package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class VehicleModelResponse {
    private Long id;
    private Long brandId;
    private String brandName;
    private Long vehicleTypeId;
    private String vehicleTypeName;
    private String name;
    private Integer tyreCount;
    private BigDecimal capacityInTons;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
