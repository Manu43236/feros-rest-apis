package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceGapsResponse {
    private Long userId;
    private String userName;
    private String roleName;
    private int totalGapDays;
    private List<LocalDate> gapDates;
}
