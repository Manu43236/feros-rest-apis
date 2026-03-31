package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String partNumber;
    private String category;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Min stock level is required")
    private Integer minStockLevel;
}
