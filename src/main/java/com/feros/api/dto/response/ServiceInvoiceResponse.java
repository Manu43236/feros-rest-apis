package com.feros.api.dto.response;

import com.feros.api.enums.ServiceInvoiceStatus;
import com.feros.api.enums.ServiceInvoiceType;
import com.feros.api.enums.VehicleServiceType;
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
public class ServiceInvoiceResponse {

    private Long id;
    private Long tenantId;
    private String invoiceNumber;
    private ServiceInvoiceType invoiceType;

    // Service snapshot
    private Long serviceId;
    private String serviceNumber;
    private String vehicleRegistrationNumber;
    private VehicleServiceType serviceType;
    private String vendorName;
    private LocalDate serviceDate;
    private LocalDate completedDate;

    // INTERNAL cost breakdown
    private List<TaskLineItem> tasks;
    private List<PartLineItem> parts;
    private BigDecimal tasksTotal;
    private BigDecimal labourCharges;
    private BigDecimal subTotal;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;

    // EXTERNAL
    private BigDecimal vendorAmount;
    private String vendorInvoiceNo;

    // Payment
    private ServiceInvoiceStatus paymentStatus;
    private LocalDateTime paidAt;
    private String paidByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Nested line-item types ───────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TaskLineItem {
        private String name;
        private BigDecimal cost;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PartLineItem {
        private String partName;
        private String partNumber;
        private String unit;
        private Integer quantity;
    }
}
