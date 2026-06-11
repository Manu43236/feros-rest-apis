package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BulkPayrollResult {

    private int totalRequested;
    private int successCount;
    private int failedCount;
    private List<PayrollResponse> succeeded;
    private List<FailedEntry> failed;

    @Getter
    @Builder
    public static class FailedEntry {
        private Long userId;
        private String userName;
        private String reason;
    }
}
