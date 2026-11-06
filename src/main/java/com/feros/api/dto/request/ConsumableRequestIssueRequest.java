package com.feros.api.dto.request;

import lombok.Data;

@Data
public class ConsumableRequestIssueRequest {
    private Long sparePartId;
    private Integer quantity;
    private String notes;
}
