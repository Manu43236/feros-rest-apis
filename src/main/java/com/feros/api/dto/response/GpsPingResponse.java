package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GpsPingResponse {
    private Long       id;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speedKmh;
    private Integer    heading;
    private Boolean    ignitionOn;
    private String     recordedAtIst;
    private String     packetType;
    private Integer    alertId;
}
