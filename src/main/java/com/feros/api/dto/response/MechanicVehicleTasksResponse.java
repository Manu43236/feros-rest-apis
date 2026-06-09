package com.feros.api.dto.response;

import com.feros.api.enums.ServicePartStatus;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MechanicVehicleTasksResponse {

    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Long serviceId;
    private String serviceNumber;
    private ServiceTriggeredBy triggeredBy;
    private String serviceLocation;
    private ServiceStatus serviceStatus;
    // breakdown info (populated when triggered by breakdown)
    private Long breakdownId;
    private String breakdownType;
    private List<MechanicTaskItem> tasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MechanicTaskItem {
        private Long taskId;
        private String displayName;
        private ServiceTaskStatus status;
        private LocalDateTime mechanicStartedAt;
        private LocalDateTime mechanicClosedAt;
        private List<PartItem> parts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartItem {
        private Long partId;
        private String partName;
        private String partNumber;
        private Integer quantityRequested;
        private Integer quantityApproved;
        private ServicePartStatus status;
    }
}
