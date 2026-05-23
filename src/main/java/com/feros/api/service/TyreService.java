package com.feros.api.service;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;

import java.util.List;

public interface TyreService {

    // Tyre CRUD
    TyreResponse createTyre(TyreRequest request);
    List<TyreResponse> getAllTyres();
    List<TyreResponse> getAvailableTyres();
    TyreResponse updateTyre(Long id, TyreRequest request);
    TyreResponse markBackToStock(Long id);

    // Positions
    TyrePositionResponse addPosition(TyrePositionRequest request);
    List<TyrePositionResponse> getPositionsForVehicle(Long vehicleId);
    List<TyrePositionResponse> getCurrentPositionsForVehicle(Long vehicleId);
    TyrePositionResponse updatePosition(Long id, TyrePositionRequest request);
    void deletePosition(Long id);

    // Fittings
    TyreFittingResponse fitTyre(TyreFitRequest request);
    TyreFittingResponse removeTyre(Long fittingId, TyreRemoveRequest request);
    List<TyreFittingResponse> getFittingHistory(Long vehicleId);
    List<TyreFittingResponse> getTyreHistory(Long tyreId);

    // Rotations
    TyreRotationLogResponse performRotation(TyreRotationRequest request);
    List<TyreRotationLogResponse> getRotationHistory(Long vehicleId);
}
