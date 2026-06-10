package com.feros.api.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TyreScrapRequest {
    private String scrapReason;
    private LocalDate scrapDate;
    private String notes;
}
