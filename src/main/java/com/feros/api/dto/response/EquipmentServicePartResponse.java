package com.feros.api.dto.response;

import com.feros.api.enums.ServicePartStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentServicePartResponse {
    private Long id;
    private Long taskId;
    private Long sparePartId;
    private String sparePartName;
    private String partNumber;
    private String unit;
    private Integer quantityRequested;
    private Integer quantityApproved;
    private ServicePartStatus status;
    private String rejectionReason;
    private String requestedByName;
    private LocalDateTime createdAt;
    // Store-keeper approval context
    private Integer availableStock;
    private String serviceNumber;
    private String equipmentName;
}
