package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GpsRoutePointResponse {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer    heading;
    private BigDecimal speedKmh;
    private String     recordedAtIst;
}
