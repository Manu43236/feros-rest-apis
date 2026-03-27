package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImageResponse {
    private Long id;
    private String imageUrl;
    private String caption;
    private LocalDateTime createdAt;
}
