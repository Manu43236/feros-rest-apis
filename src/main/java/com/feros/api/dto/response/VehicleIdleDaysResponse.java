package com.feros.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleIdleDaysResponse {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int totalDays;
    private int activeDays;
    private int idleDays;
    private double idlePct;
}
