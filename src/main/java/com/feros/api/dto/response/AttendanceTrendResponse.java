package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceTrendResponse {
    private LocalDate date;
    private int presentCount;
    private int absentCount;
    private int leaveCount;
    private int notMarkedCount;
    private int totalStaff;
}
