package com.feros.api.service;

import com.feros.api.dto.response.UserResponse;
import com.feros.api.dto.response.VehicleResponse;

import java.util.List;
import java.util.Set;

public interface SupervisorWatchlistService {

    // Vehicle watchlist
    List<VehicleResponse> getVehicleWatchlist();
    VehicleResponse addVehicleToWatchlist(Long vehicleId);
    void removeVehicleFromWatchlist(Long vehicleId);
    Set<Long> getWatchlistedVehicleIds();

    // Staff watchlist
    List<UserResponse> getStaffWatchlist();
    UserResponse addStaffToWatchlist(Long userId);
    void removeStaffFromWatchlist(Long userId);
    Set<Long> getWatchlistedStaffIds();
}
