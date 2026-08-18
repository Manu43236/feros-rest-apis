package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceVendorItemResponse {
    private Long id;
    private String description;
    private BigDecimal cost;
}
