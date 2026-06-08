package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignMechanicRequest {
    @NotNull(message = "mechanicId is required")
    private Long mechanicId;
}
