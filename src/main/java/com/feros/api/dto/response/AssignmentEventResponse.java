package com.feros.api.dto.response;

import com.feros.api.enums.AssignmentEventType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentEventResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long orderId;
    private String orderNumber;
    private AssignmentEventType eventType;
    private String personName;
    private String personRole;
    private Long performedById;
    private String performedByName;
    private LocalDateTime performedAt;
}
