package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteServiceRequest {
    @NotNull(message = "Completed date is required")
    private LocalDate completedDate;
    private Integer odometer;
}
