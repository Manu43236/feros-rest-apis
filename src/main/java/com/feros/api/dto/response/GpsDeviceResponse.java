package com.feros.api.dto.response;

import com.feros.api.enums.GpsConnectionType;
import com.feros.api.enums.GpsDeviceStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsDeviceResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long modelId;
    private String companyName;
    private String modelName;
    private GpsConnectionType connectionType;
    private String parserKey;
    private String deviceIdentifier;
    private GpsDeviceStatus status;
    private String notes;
    private LocalDateTime lastPingAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // credentials intentionally excluded — write-only
}
