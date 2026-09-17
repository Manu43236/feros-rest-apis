package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceRoleSummaryRow {
    private String role;
    private int staffCount;
    private int presented;
    private int absent;
}
