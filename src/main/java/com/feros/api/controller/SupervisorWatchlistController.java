package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.UserResponse;
import com.feros.api.dto.response.VehicleResponse;
import com.feros.api.service.SupervisorWatchlistService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/supervisor/watchlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERVISOR')")
public class SupervisorWatchlistController {

    private final SupervisorWatchlistService watchlistService;

    // ── Vehicle watchlist ──────────────────────────────────────────────────────

    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehicleWatchlist() {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle watchlist fetched", watchlistService.getVehicleWatchlist()));
    }

    @GetMapping("/vehicles/ids")
    public ResponseEntity<ApiResponse<Set<Long>>> getWatchlistedVehicleIds() {
        return ResponseEntity.ok(ApiResponse.success(
                "Watchlisted vehicle IDs fetched", watchlistService.getWatchlistedVehicleIds()));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(@RequestBody VehicleWatchlistRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vehicle added to watchlist", watchlistService.addVehicleToWatchlist(request.getVehicleId())));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<Void>> removeVehicle(@PathVariable Long vehicleId) {
        watchlistService.removeVehicleFromWatchlist(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle removed from watchlist", null));
    }

    // ── Staff watchlist ────────────────────────────────────────────────────────

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getStaffWatchlist() {
        return ResponseEntity.ok(ApiResponse.success(
                "Staff watchlist fetched", watchlistService.getStaffWatchlist()));
    }

    @GetMapping("/staff/ids")
    public ResponseEntity<ApiResponse<Set<Long>>> getWatchlistedStaffIds() {
        return ResponseEntity.ok(ApiResponse.success(
                "Watchlisted staff IDs fetched", watchlistService.getWatchlistedStaffIds()));
    }

    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<UserResponse>> addStaff(@RequestBody StaffWatchlistRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Staff added to watchlist", watchlistService.addStaffToWatchlist(request.getUserId())));
    }

    @DeleteMapping("/staff/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeStaff(@PathVariable Long userId) {
        watchlistService.removeStaffFromWatchlist(userId);
        return ResponseEntity.ok(ApiResponse.success("Staff removed from watchlist", null));
    }

    // ── Request bodies ─────────────────────────────────────────────────────────

    @Getter @Setter
    public static class VehicleWatchlistRequest {
        private Long vehicleId;
    }

    @Getter @Setter
    public static class StaffWatchlistRequest {
        private Long userId;
    }
}
