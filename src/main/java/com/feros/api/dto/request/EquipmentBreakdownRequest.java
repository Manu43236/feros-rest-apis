package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentBreakdownRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    // Optional — defaults to now if not supplied.
    private LocalDateTime breakdownDate;

    private String location;

    private String notes;
}
