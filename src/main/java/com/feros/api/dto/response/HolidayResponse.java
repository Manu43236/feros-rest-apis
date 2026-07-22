package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HolidayResponse {
    private Long id;
    private Long tenantId;
    private LocalDate holidayDate;
    private String holidayName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
