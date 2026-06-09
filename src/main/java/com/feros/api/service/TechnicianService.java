package com.feros.api.service;

import com.feros.api.dto.request.ServicePartRequest;
import com.feros.api.dto.response.TechnicianVehicleTasksResponse;
import com.feros.api.dto.response.ServicePartResponse;

import java.util.List;

public interface TechnicianService {
    List<TechnicianVehicleTasksResponse> getMyTasks();
    TechnicianVehicleTasksResponse startTask(Long taskId);
    TechnicianVehicleTasksResponse closeTask(Long taskId);
    ServicePartResponse requestSparePart(Long taskId, ServicePartRequest request);
}
