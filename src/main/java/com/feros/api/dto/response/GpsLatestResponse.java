package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GpsLatestResponse {
    private Long       vehicleId;
    private String     registrationNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speedKmh;
    private Integer    heading;
    private Boolean    ignitionOn;
    private String     lastPingUtc;
    private String     lastPingIst;
    private Boolean    isLive;
}
