package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterResponse {
    private Long id;
    private String name;
    private String code;
    private String category;
    private String applicableFor;
    private BigDecimal capacityInTons;
    private Integer tyreCount;
    private BigDecimal rate;
    private String taxType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}