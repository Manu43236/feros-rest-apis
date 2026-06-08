package com.feros.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePartRequest {

    @NotNull(message = "Spare part ID is required")
    private Long sparePartId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityRequested;

    /** Optional: the specific task this part is being requested for. */
    private Long taskId;
}
