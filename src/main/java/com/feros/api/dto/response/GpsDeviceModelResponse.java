package com.feros.api.dto.response;

import com.feros.api.enums.GpsConnectionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsDeviceModelResponse {
    private Long id;
    private String companyName;
    private String modelName;
    private GpsConnectionType connectionType;
    private String parserKey;
    private String protocolVersion;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
