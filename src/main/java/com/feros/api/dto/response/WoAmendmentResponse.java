package com.feros.api.dto.response;

import com.feros.api.enums.AmendmentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WoAmendmentResponse {
    private Long id;
    private Long workOrderId;
    private AmendmentType amendmentType;
    private LocalDate effectiveDate;
    private String oldValue;
    private String newValue;
    private String reason;
    private String createdBy;
    private LocalDateTime createdAt;
}
