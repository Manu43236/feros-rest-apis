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
    private String driverName;   // shift vehicles: "S1 <name> · S2 <name> · S3 <name>"
    private String cleanerName;  // same, per-shift
    private boolean isLease;     // vehicle is on an active lease (show a "Lease" tag)
}
