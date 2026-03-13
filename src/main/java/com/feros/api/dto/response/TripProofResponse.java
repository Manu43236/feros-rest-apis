package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripProofResponse {
    private Long id;
    private Long lrId;
    private String lrNumber;
    private Long userId;
    private String userName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String imageUrl;
    private LocalDateTime capturedAt;
    private Boolean isReviewed;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}