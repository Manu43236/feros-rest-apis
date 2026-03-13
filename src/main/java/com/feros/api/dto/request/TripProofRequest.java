package com.feros.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TripProofRequest {

    @NotNull(message = "LR is required")
    private Long lrId;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    @NotNull(message = "Image URL is required")
    private String imageUrl;

    private LocalDateTime capturedAt;
}