package com.feros.api.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkTenantUploadResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<String> errors;
}