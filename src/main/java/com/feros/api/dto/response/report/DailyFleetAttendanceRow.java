package com.feros.api.dto.response.report;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyFleetAttendanceRow {
    private String registrationNumber;
    private String scope;
    private String vehicleType;
    private String driverName;
    private String cleanerName;
}
