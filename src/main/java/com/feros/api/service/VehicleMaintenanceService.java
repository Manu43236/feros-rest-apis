package com.feros.api.service;

import com.feros.api.dto.request.CompleteServiceRequest;
import com.feros.api.dto.request.VehicleServiceRequest;
import com.feros.api.dto.request.VehicleServiceTaskRequest;
import com.feros.api.dto.response.VehicleServiceResponse;

import java.util.List;

public interface VehicleMaintenanceService {
    VehicleServiceResponse create(VehicleServiceRequest request);
    List<VehicleServiceResponse> getAll();
    List<VehicleServiceResponse> getByVehicle(Long vehicleId);
    VehicleServiceResponse getById(Long id);
    VehicleServiceResponse start(Long id);
    VehicleServiceResponse cancel(Long id);
    VehicleServiceResponse updateNotes(Long id, String notes);
    VehicleServiceResponse complete(Long id, CompleteServiceRequest request);
    VehicleServiceResponse completeTask(Long serviceId, Long taskId);
    VehicleServiceResponse assignTask(Long serviceId, Long taskId, Long mechanicId);
    VehicleServiceResponse addTask(Long serviceId, VehicleServiceTaskRequest request);
    void delete(Long id);
}
