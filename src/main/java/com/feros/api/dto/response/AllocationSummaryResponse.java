package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Per-order vehicle allocation breakdown by stage. Add-on only — nullable on {@link OrderResponse}.
 * cancelled includes BREAKDOWN-replaced rows (both soft-deleted, so they are not in the active list).
 */
@Data
@Builder
public class AllocationSummaryResponse {
    private int allocated;   // assigned, no LR yet
    private int inTrip;      // LR created / in transit
    private int delivered;   // delivered, not yet on an invoice
    private int invoiced;    // delivered and billed
    private int cancelled;   // cancelled or breakdown-replaced
    private int total;
}
