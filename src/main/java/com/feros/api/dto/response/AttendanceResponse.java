package com.feros.api.dto.response;

import com.feros.api.enums.AttendanceApprovalStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String roleName;
    private LocalDate attendanceDate;
    private Long attendanceTypeId;
    private String attendanceTypeName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private String leaveReason;
    private Long markedById;
    private String markedByName;
    private LocalDateTime markedAt;
    private LocalDateTime markedOutAt;
    private String dutyLabel;
    private Double dutyHours;
    private boolean canUndoOut;
    private String remarks;
    private AttendanceApprovalStatus approvalStatus;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String assignedVehicleNumber;
    private String selfieUrl;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}