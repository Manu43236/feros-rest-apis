package com.feros.api.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCalendarResponse {
    private int year;
    private int month;
    private int daysInMonth;
    private List<UserCalendarRow> users;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCalendarRow {
        private Long userId;
        private String userName;
        private String roleName;
        private Map<Integer, String> dailyStatus; // day (1-31) -> status
    }
}
