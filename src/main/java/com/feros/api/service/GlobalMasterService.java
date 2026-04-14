package com.feros.api.service;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;

import java.util.List;


public interface GlobalMasterService {

    // States
    StateResponse createState(StateRequest request);
    StateResponse getStateById(Long id);
    List<StateResponse> getAllStates();
    StateResponse updateState(Long id, StateRequest request);
    void deleteState(Long id);

    // Cities
    CityResponse createCity(CityRequest request);
    CityResponse getCityById(Long id);
    List<CityResponse> getAllCities();
    List<CityResponse> getCitiesByState(Long stateId);
    CityResponse updateCity(Long id, CityRequest request);
    void deleteCity(Long id);

    // Vehicle Brands
    MasterResponse createVehicleBrand(MasterRequest request);
    MasterResponse getVehicleBrandById(Long id);
    List<MasterResponse> getAllVehicleBrands();
    MasterResponse updateVehicleBrand(Long id, MasterRequest request);
    void deleteVehicleBrand(Long id);

    // Vehicle Types
    MasterResponse createVehicleType(VehicleTypeRequest request);
    MasterResponse getVehicleTypeById(Long id);
    List<MasterResponse> getAllVehicleTypes();
    MasterResponse updateVehicleType(Long id, VehicleTypeRequest request);
    void deleteVehicleType(Long id);

    // Fuel Types
    MasterResponse createFuelType(MasterRequest request);
    MasterResponse getFuelTypeById(Long id);
    List<MasterResponse> getAllFuelTypes();
    MasterResponse updateFuelType(Long id, MasterRequest request);
    void deleteFuelType(Long id);

    // Material Types
    MasterResponse createMaterialType(MaterialTypeRequest request);
    MasterResponse getMaterialTypeById(Long id);
    List<MasterResponse> getAllMaterialTypes();
    MasterResponse updateMaterialType(Long id, MaterialTypeRequest request);
    void deleteMaterialType(Long id);

    // Document Types
    MasterResponse createDocumentType(DocumentTypeRequest request);
    MasterResponse getDocumentTypeById(Long id);
    List<MasterResponse> getAllDocumentTypes();
    MasterResponse updateDocumentType(Long id, DocumentTypeRequest request);
    void deleteDocumentType(Long id);

    // Attendance Types
    MasterResponse createAttendanceType(MasterRequest request);
    MasterResponse getAttendanceTypeById(Long id);
    List<MasterResponse> getAllAttendanceTypes();
    MasterResponse updateAttendanceType(Long id, MasterRequest request);
    void deleteAttendanceType(Long id);

    // Leave Types
    MasterResponse createLeaveType(MasterRequest request);
    MasterResponse getLeaveTypeById(Long id);
    List<MasterResponse> getAllLeaveTypes();
    MasterResponse updateLeaveType(Long id, MasterRequest request);
    void deleteLeaveType(Long id);

    // Employment Types
    MasterResponse createEmploymentType(MasterRequest request);
    MasterResponse getEmploymentTypeById(Long id);
    List<MasterResponse> getAllEmploymentTypes();
    MasterResponse updateEmploymentType(Long id, MasterRequest request);
    void deleteEmploymentType(Long id);

    // Ownership Types
    MasterResponse createOwnershipType(MasterRequest request);
    MasterResponse getOwnershipTypeById(Long id);
    List<MasterResponse> getAllOwnershipTypes();
    MasterResponse updateOwnershipType(Long id, MasterRequest request);
    void deleteOwnershipType(Long id);

    // Taxes
    MasterResponse createTax(TaxRequest request);
    MasterResponse getTaxById(Long id);
    List<MasterResponse> getAllTaxes();
    MasterResponse updateTax(Long id, TaxRequest request);
    void deleteTax(Long id);

    // Deduction Types
    MasterResponse createDeductionType(MasterRequest request);
    MasterResponse getDeductionTypeById(Long id);
    List<MasterResponse> getAllDeductionTypes();
    MasterResponse updateDeductionType(Long id, MasterRequest request);
    void deleteDeductionType(Long id);

    // Payment Statuses
    MasterResponse createPaymentStatus(MasterRequest request);
    MasterResponse getPaymentStatusById(Long id);
    List<MasterResponse> getAllPaymentStatuses();
    MasterResponse updatePaymentStatus(Long id, MasterRequest request);
    void deletePaymentStatus(Long id);

    // Service Task Types
    MasterResponse createServiceTaskType(MasterRequest request);
    List<MasterResponse> getAllServiceTaskTypes();
    MasterResponse updateServiceTaskType(Long id, MasterRequest request);
    void deleteServiceTaskType(Long id);

    // Vehicle Models
    VehicleModelResponse createVehicleModel(VehicleModelRequest request);
    List<VehicleModelResponse> getAllVehicleModels();
    List<VehicleModelResponse> getVehicleModelsByBrand(Long brandId);
    VehicleModelResponse updateVehicleModel(Long id, VehicleModelRequest request);
    void deleteVehicleModel(Long id);
}