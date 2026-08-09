package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VehicleCurrentStaffResponse {
    private StaffMember driver;
    private StaffMember cleaner;

    @Builder
    @Getter
    public static class StaffMember {
        private Long id;
        private String name;
        private String phone;
        private boolean hasAttendanceToday;
    }
}
