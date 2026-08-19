package com.feros.api.dto.response;

import com.feros.api.enums.BreakdownStatus;
import com.feros.api.enums.BreakdownType;
import com.feros.api.enums.ServicePartStatus;
import com.feros.api.enums.ServiceStatus;
import com.feros.api.enums.ServiceTaskStatus;
import com.feros.api.enums.ServiceTriggeredBy;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceManagerDashboardResponse {

    private List<BreakdownItem> breakdowns;
    private List<ServiceItem> generalServices;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BreakdownItem {
        private Long breakdownId;
        private Long vehicleId;
        private String vehicleRegistrationNumber;
        private BreakdownType breakdownType;
        private String reason;   // supervisor's breakdown description
        private String notes;
        private String location;
        private BreakdownStatus breakdownStatus;
        private LocalDateTime breakdownDate;
        // linked service (null if not yet logged)
        private ServiceItem service;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceItem {
        private Long serviceId;
        private String serviceNumber;
        private Long vehicleId;
        private String vehicleRegistrationNumber;
        private ServiceStatus serviceStatus;
        private String displayStatus;
        private LocalDate serviceDate;
        private ServiceTriggeredBy triggeredBy;
        private com.feros.api.enums.VehicleServiceType serviceType;
        private String vendorName;
        private String location;
        private String notes;
        private BigDecimal estimatedCost;
        private BigDecimal completedCost;
        private BigDecimal totalCost;
        private String estimateDocUrl;
        private String billDocUrl;
        private List<VendorItemDto> vendorItems;
        private int tasksTotal;
        private int tasksAssigned;
        private int tasksMechanicClosed;
        private List<TaskItem> tasks;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VendorItemDto {
        private Long id;
        private String description;
        private BigDecimal cost;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskItem {
        private Long taskId;
        private String displayName;
        private ServiceTaskStatus status;
        private BigDecimal cost;
        private Long assignedMechanicId;
        private String assignedMechanicName;
        private java.time.LocalDateTime mechanicStartedAt;
        private java.time.LocalDateTime mechanicClosedAt;
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
