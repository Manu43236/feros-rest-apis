package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderLeadTimeResponse {
    private String fromCity;
    private String toCity;
    private int orderCount;
    private double avgLeadTimeDays;
    private double minLeadTimeDays;
    private double maxLeadTimeDays;
}
