package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffRoleStatRow {
    private String role;
    private int addedThisMonth;
    private int deactivatedThisMonth;
    private int totalPreviousMonth;
}
