package com.feros.api.service;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;

import java.util.List;

public interface TireService {

    // Tire CRUD
    TireResponse createTire(TireRequest request);
    List<TireResponse> getAllTires();
    List<TireResponse> getAvailableTires();
    TireResponse updateTire(Long id, TireRequest request);

    // Positions
    TirePositionResponse addPosition(TirePositionRequest request);
    List<TirePositionResponse> getPositionsForVehicle(Long vehicleId);
    List<TirePositionResponse> getCurrentPositionsForVehicle(Long vehicleId);
    TirePositionResponse updatePosition(Long id, TirePositionRequest request);
    void deletePosition(Long id);

    // Fittings
    TireFittingResponse fitTire(TireFitRequest request);
    TireFittingResponse removeTire(Long fittingId, TireRemoveRequest request);
    List<TireFittingResponse> getFittingHistory(Long vehicleId);
    List<TireFittingResponse> getTireHistory(Long tireId);

    // Rotations
    TireRotationLogResponse performRotation(TireRotationRequest request);
    List<TireRotationLogResponse> getRotationHistory(Long vehicleId);
}
