package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String partNumber;
    private String category;
    private String unit;
    private Integer minStockLevel;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
