package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverPerformanceResponse {
    private Long userId;
    private String driverName;
    private String phone;
    private String roleName;
    private int tripCount;
    private BigDecimal totalLoadedTons;
    private BigDecimal totalDeliveredTons;
}
