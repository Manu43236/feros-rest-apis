package com.feros.api.service;

import com.feros.api.dto.request.CompleteServiceRequest;
import com.feros.api.dto.request.VehicleServiceRequest;
import com.feros.api.dto.request.VehicleServiceTaskRequest;
import com.feros.api.dto.response.VehicleServiceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface VehicleMaintenanceService {
    VehicleServiceResponse create(VehicleServiceRequest request);
    List<VehicleServiceResponse> getAll();
    List<VehicleServiceResponse> getByVehicle(Long vehicleId);
    VehicleServiceResponse getById(Long id);
    VehicleServiceResponse start(Long id);
    VehicleServiceResponse cancel(Long id);
    VehicleServiceResponse updateNotes(Long id, String notes);
    VehicleServiceResponse updateServiceCharges(Long id, BigDecimal serviceCharges);
    VehicleServiceResponse uploadEstimateDoc(Long id, MultipartFile file) throws IOException;
    VehicleServiceResponse uploadBillDoc(Long id, MultipartFile file) throws IOException;
    VehicleServiceResponse complete(Long id, CompleteServiceRequest request);
    VehicleServiceResponse completeTask(Long serviceId, Long taskId);
    VehicleServiceResponse assignTask(Long serviceId, Long taskId, Long mechanicId);
    VehicleServiceResponse addTask(Long serviceId, VehicleServiceTaskRequest request);
    void delete(Long id);
}
