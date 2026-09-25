package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Paginated vehicle services plus fleet-wide summary. The summary (totals + status
 * counts) is computed over the full tenant set so the stat cards and filter pills
 * stay correct even though only one page of records is returned.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedVehicleServiceResponse {
    private List<VehicleServiceResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private Summary summary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private long totalRecords;
        private BigDecimal totalCost;
        private long inProgress;
        private long dueSoon;
        private long overdue;
    }
}
