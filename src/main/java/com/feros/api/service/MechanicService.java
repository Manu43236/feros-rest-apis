package com.feros.api.service;

import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.MechanicVehicleTasksResponse;
import com.feros.api.dto.response.ServicePartResponse;

import java.util.List;

public interface MechanicService {
    List<MechanicVehicleTasksResponse> getMyTasks();
    MechanicVehicleTasksResponse startTask(Long taskId);
    MechanicVehicleTasksResponse closeTask(Long taskId);
    ServicePartResponse requestSparePart(Long taskId, ServicePartRequest request);
}
