package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LrCheckpostResponse {
    private Long id;
    private String checkpostName;
    private String location;
    private BigDecimal fineAmount;
    private String fineReceiptNumber;
    private LocalDateTime finePaidAt;
    private String remarks;
    private LocalDateTime createdAt;
}