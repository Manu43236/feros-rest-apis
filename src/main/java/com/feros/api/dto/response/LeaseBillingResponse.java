package com.feros.api.dto.response;

import com.feros.api.enums.RateType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class LeaseBillingResponse {

    private Long leaseId;
    private String leaseNumber;
    private RateType rateType;
    private List<VehicleBillingLine> lines;
    private BigDecimal totalAmount;

    @Getter
    @Builder
    public static class VehicleBillingLine {
        private Long assignmentId;
        private String registrationNumber;
        private BigDecimal ratePerVehicle;
        private long days;
        private BigDecimal amount;
    }
}
