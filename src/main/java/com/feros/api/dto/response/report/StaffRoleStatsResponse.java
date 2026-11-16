package com.feros.api.dto.response.report;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StaffRoleStatsResponse {
    private String currentMonthLabel;   // e.g. "2026-11"
    private String previousMonthLabel;  // e.g. "2026-10"
    private List<StaffRoleStatRow> roleStats;
    private List<StaffDirectoryRow> newJoiners;  // joined this month
}
