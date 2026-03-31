package com.feros.api.dto.request;

import com.feros.api.enums.ServicePartStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePartApprovalRequest {

    @NotNull(message = "Status is required")
    private ServicePartStatus status; // APPROVED or REJECTED

    private Integer quantityApproved; // required if APPROVED
    private String rejectionReason;   // required if REJECTED
}
