package com.feros.api.dto.response;

import com.feros.api.enums.StaffAllocationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffAllocationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String roleName;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private StaffAllocationStatus allocationStatus;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}