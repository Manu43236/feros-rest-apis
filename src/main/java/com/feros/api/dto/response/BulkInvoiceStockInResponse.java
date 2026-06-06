package com.feros.api.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkInvoiceStockInResponse {

    private int totalItems;
    private int savedCount;
    private int failedCount;
    private List<SavedItem> saved;
    private List<FailedItem> failed;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SavedItem {
        private Long sparePartId;
        private String partName;
        private int quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FailedItem {
        private int lineIndex; // 1-based
        private Long sparePartId;
        private String reason;
    }
}
