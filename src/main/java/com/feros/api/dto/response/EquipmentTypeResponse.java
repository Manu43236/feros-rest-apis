package com.feros.api.dto.response;

import com.feros.api.enums.MeterType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentTypeResponse {
    private Long id;
    private String name;
    private Long modelId;
    private String modelName;
    private Long makeId;
    private String makeName;
    private MeterType defaultMeterType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
