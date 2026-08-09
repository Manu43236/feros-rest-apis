package com.feros.api.service;

import com.feros.api.dto.request.BreakdownRequest;
import com.feros.api.dto.request.BreakdownReplaceRequest;
import com.feros.api.dto.response.BreakdownResponse;
import com.feros.api.dto.response.VehicleCurrentStaffResponse;

import java.util.List;

public interface VehicleBreakdownService {

    // Admin / Supervisor / OfficeStaff — explicit orderId + allocationId
    BreakdownResponse reportBreakdown(Long orderId, Long allocationId, BreakdownRequest request);

    // Driver — auto-detects their active IN_TRANSIT allocation from JWT
    BreakdownResponse reportMyBreakdown(BreakdownRequest request);

    // Assign replacement vehicle, reassign LR, optionally transfer staff
    BreakdownResponse replaceVehicle(Long orderId, Long breakdownId, BreakdownReplaceRequest request);

    // Vehicle repaired on road — trip continues
    BreakdownResponse resolveBreakdown(Long orderId, Long breakdownId);

    // Cancel false alarm — only when REPORTED and no replacement assigned yet
    void cancelBreakdown(Long orderId, Long breakdownId);

    // Get breakdown for a specific vehicle allocation
    BreakdownResponse getBreakdownByAllocation(Long orderId, Long allocationId);

    // Full breakdown history for a vehicle (maintenance view)
    List<BreakdownResponse> getBreakdownHistoryByVehicle(Long vehicleId);

    // All breakdowns for the tenant (service men mobile view)
    List<BreakdownResponse> getAllBreakdowns();

    // Standalone — mark an AVAILABLE vehicle as BREAKDOWN (not on any order)
    BreakdownResponse reportStandaloneBreakdown(Long vehicleId, BreakdownRequest request);

    // Standalone — resolve a standalone breakdown, restore vehicle to AVAILABLE
    BreakdownResponse resolveStandaloneBreakdown(Long vehicleId, Long breakdownId);

    // Return V2's most recent staff with today's attendance status (for the replace dialog)
    VehicleCurrentStaffResponse getReplacementVehicleStaff(Long vehicleId);
}
