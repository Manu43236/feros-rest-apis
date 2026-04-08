package com.feros.api.dto.response;

import com.feros.api.enums.MeterReadingType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MeterReadingResponse {

    private Long id;
    private Long tenantId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private BigDecimal readingKm;
    private MeterReadingType readingType;
    private Long lrId;
    private String lrNumber;
    private String photoUrl;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime recordedAt;
    private String notes;
    private LocalDateTime createdAt;

    // Service alerts triggered by this reading
    private List<ServiceAlertDto> serviceAlerts;

    @Data
    @Builder
    public static class ServiceAlertDto {
        private Long serviceId;
        private String serviceNumber;
        private String serviceType;
        private Integer dueAtOdometer;
        private String status; // DUE_SOON or OVERDUE
    }
}
