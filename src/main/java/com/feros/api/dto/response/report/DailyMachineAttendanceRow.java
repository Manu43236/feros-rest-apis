package com.feros.api.dto.response.report;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMachineAttendanceRow {
    private String registrationNumber;
    private String equipmentType;
    private String operatorName; // shift machines: "S1 <name> · S2 <name>"; client crew tagged "(Client)"
    private String cleanerName;  // same, per-shift
}
