package com.feros.api.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownFrequencyResponse {
    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private int breakdownCount;
    private List<String> breakdownTypes;
    private String lastBreakdownDate;
}
