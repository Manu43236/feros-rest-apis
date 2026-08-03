package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StaffAssignmentHistoryResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long userId;
    private String userName;
    private String userRole;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private String unassignedByName;
    private LocalDateTime unassignedAt;
}
