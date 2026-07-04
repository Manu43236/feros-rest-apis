package com.feros.api.controller;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;
import com.feros.api.repository.RoleRepository;
import com.feros.api.service.GlobalMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/masters/global")
@RequiredArgsConstructor
public class GlobalMasterController {

    private final GlobalMasterService globalMasterService;
    private final RoleRepository roleRepository;

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllRoles() {
        List<Map<String, Object>> roles = roleRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .map(r -> Map.<String, Object>of(
                        "id",          r.getId(),
                        "name",        r.getName().name(),
                        "description", r.getDescription() != null ? r.getDescription() : ""
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", roles));
    }

    // ===================== STATES =====================
    @GetMapping("/states")
    public ResponseEntity<?> getAllStates(
            @RequestParam(defaultValue = "-1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search) {
        if (page < 0) {
            return ResponseEntity.ok(ApiResponse.success("States fetched successfully", globalMasterService.getAllStates()));
        }
        return ResponseEntity.ok(ApiResponse.success("States fetched successfully", globalMasterService.getStatesPaged(page, size, search)));
    }

    @GetMapping("/states/{id}")
    public ResponseEntity<ApiResponse<StateResponse>> getStateById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("State fetched successfully", globalMasterService.getStateById(id)));
    }

    @PostMapping("/states")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StateResponse>> createState(@Valid @RequestBody StateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("State created successfully", globalMasterService.createState(request)));
    }

    @PutMapping("/states/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StateResponse>> updateState(@PathVariable Long id, @Valid @RequestBody StateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("State updated successfully", globalMasterService.updateState(id, request)));
    }

    @DeleteMapping("/states/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteState(@PathVariable Long id) {
        globalMasterService.deleteState(id);
        return ResponseEntity.ok(ApiResponse.success("State deleted successfully", null));
    }

    // ===================== CITIES =====================
    @GetMapping("/cities")
    public ResponseEntity<?> getAllCities(
            @RequestParam(defaultValue = "-1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long stateId) {
        if (page < 0) {
            // Non-paginated: filter by stateId if provided (used by mobile/web dropdowns)
            if (stateId != null) {
                return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", globalMasterService.getCitiesByState(stateId)));
            }
            return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", globalMasterService.getAllCities()));
        }
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", globalMasterService.getCitiesPaged(page, size, search, stateId)));
    }

    @GetMapping("/cities/{id}")
    public ResponseEntity<ApiResponse<CityResponse>> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("City fetched successfully", globalMasterService.getCityById(id)));
    }

    @GetMapping("/cities/state/{stateId}")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getCitiesByState(@PathVariable Long stateId) {
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", globalMasterService.getCitiesByState(stateId)));
    }

    @PostMapping("/cities")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> createCity(@Valid @RequestBody CityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("City created successfully", globalMasterService.createCity(request)));
    }

    @PutMapping("/cities/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> updateCity(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("City updated successfully", globalMasterService.updateCity(id, request)));
    }

    @DeleteMapping("/cities/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
        globalMasterService.deleteCity(id);
        return ResponseEntity.ok(ApiResponse.success("City deleted successfully", null));
    }

    // ===================== VEHICLE BRANDS =====================
    @GetMapping("/vehicle-brands")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllVehicleBrands() {
        return ResponseEntity.ok(ApiResponse.success("Vehicle brands fetched successfully", globalMasterService.getAllVehicleBrands()));
    }

    @GetMapping("/vehicle-brands/{id}")
    public ResponseEntity<ApiResponse<MasterResponse>> getVehicleBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle brand fetched successfully", globalMasterService.getVehicleBrandById(id)));
    }

    @PostMapping("/vehicle-brands")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createVehicleBrand(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle brand created successfully", globalMasterService.createVehicleBrand(request)));
    }

    @PutMapping("/vehicle-brands/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateVehicleBrand(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle brand updated successfully", globalMasterService.updateVehicleBrand(id, request)));
    }

    @DeleteMapping("/vehicle-brands/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleBrand(@PathVariable Long id) {
        globalMasterService.deleteVehicleBrand(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle brand deleted successfully", null));
    }

    // ===================== VEHICLE TYPES =====================
    @GetMapping("/vehicle-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllVehicleTypes() {
        return ResponseEntity.ok(ApiResponse.success("Vehicle types fetched successfully", globalMasterService.getAllVehicleTypes()));
    }

    @GetMapping("/vehicle-types/{id}")
    public ResponseEntity<ApiResponse<MasterResponse>> getVehicleTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle type fetched successfully", globalMasterService.getVehicleTypeById(id)));
    }

    @PostMapping("/vehicle-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createVehicleType(@Valid @RequestBody VehicleTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle type created successfully", globalMasterService.createVehicleType(request)));
    }

    @PutMapping("/vehicle-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateVehicleType(@PathVariable Long id, @Valid @RequestBody VehicleTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle type updated successfully", globalMasterService.updateVehicleType(id, request)));
    }

    @DeleteMapping("/vehicle-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleType(@PathVariable Long id) {
        globalMasterService.deleteVehicleType(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle type deleted successfully", null));
    }

    // ===================== FUEL TYPES =====================
    @GetMapping("/fuel-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllFuelTypes() {
        return ResponseEntity.ok(ApiResponse.success("Fuel types fetched successfully", globalMasterService.getAllFuelTypes()));
    }

    @GetMapping("/fuel-types/{id}")
    public ResponseEntity<ApiResponse<MasterResponse>> getFuelTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Fuel type fetched successfully", globalMasterService.getFuelTypeById(id)));
    }

    @PostMapping("/fuel-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createFuelType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fuel type created successfully", globalMasterService.createFuelType(request)));
    }

    @PutMapping("/fuel-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateFuelType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fuel type updated successfully", globalMasterService.updateFuelType(id, request)));
    }

    @DeleteMapping("/fuel-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFuelType(@PathVariable Long id) {
        globalMasterService.deleteFuelType(id);
        return ResponseEntity.ok(ApiResponse.success("Fuel type deleted successfully", null));
    }

    // ===================== MATERIAL TYPES =====================
    @GetMapping("/material-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllMaterialTypes() {
        return ResponseEntity.ok(ApiResponse.success("Material types fetched successfully", globalMasterService.getAllMaterialTypes()));
    }

    @GetMapping("/material-types/{id}")
    public ResponseEntity<ApiResponse<MasterResponse>> getMaterialTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Material type fetched successfully", globalMasterService.getMaterialTypeById(id)));
    }

    @PostMapping("/material-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createMaterialType(@Valid @RequestBody MaterialTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Material type created successfully", globalMasterService.createMaterialType(request)));
    }

    @PutMapping("/material-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateMaterialType(@PathVariable Long id, @Valid @RequestBody MaterialTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Material type updated successfully", globalMasterService.updateMaterialType(id, request)));
    }

    @DeleteMapping("/material-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMaterialType(@PathVariable Long id) {
        globalMasterService.deleteMaterialType(id);
        return ResponseEntity.ok(ApiResponse.success("Material type deleted successfully", null));
    }

    // ===================== DOCUMENT TYPES =====================
    @GetMapping("/document-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllDocumentTypes() {
        return ResponseEntity.ok(ApiResponse.success("Document types fetched successfully", globalMasterService.getAllDocumentTypes()));
    }

    @GetMapping("/document-types/{id}")
    public ResponseEntity<ApiResponse<MasterResponse>> getDocumentTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Document type fetched successfully", globalMasterService.getDocumentTypeById(id)));
    }

    @PostMapping("/document-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createDocumentType(@Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Document type created successfully", globalMasterService.createDocumentType(request)));
    }

    @PutMapping("/document-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateDocumentType(@PathVariable Long id, @Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Document type updated successfully", globalMasterService.updateDocumentType(id, request)));
    }

    @DeleteMapping("/document-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentType(@PathVariable Long id) {
        globalMasterService.deleteDocumentType(id);
        return ResponseEntity.ok(ApiResponse.success("Document type deleted successfully", null));
    }

    // ===================== ATTENDANCE TYPES =====================
    @GetMapping("/attendance-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllAttendanceTypes() {
        return ResponseEntity.ok(ApiResponse.success("Attendance types fetched successfully", globalMasterService.getAllAttendanceTypes()));
    }

    @PostMapping("/attendance-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createAttendanceType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance type created successfully", globalMasterService.createAttendanceType(request)));
    }

    @PutMapping("/attendance-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateAttendanceType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance type updated successfully", globalMasterService.updateAttendanceType(id, request)));
    }

    @DeleteMapping("/attendance-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAttendanceType(@PathVariable Long id) {
        globalMasterService.deleteAttendanceType(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance type deleted successfully", null));
    }

    // ===================== LEAVE TYPES =====================
    @GetMapping("/leave-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllLeaveTypes() {
        return ResponseEntity.ok(ApiResponse.success("Leave types fetched successfully", globalMasterService.getAllLeaveTypes()));
    }

    @PostMapping("/leave-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createLeaveType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Leave type created successfully", globalMasterService.createLeaveType(request)));
    }

    @PutMapping("/leave-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateLeaveType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Leave type updated successfully", globalMasterService.updateLeaveType(id, request)));
    }

    @DeleteMapping("/leave-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveType(@PathVariable Long id) {
        globalMasterService.deleteLeaveType(id);
        return ResponseEntity.ok(ApiResponse.success("Leave type deleted successfully", null));
    }

    // ===================== EMPLOYMENT TYPES =====================
    @GetMapping("/employment-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllEmploymentTypes() {
        return ResponseEntity.ok(ApiResponse.success("Employment types fetched successfully", globalMasterService.getAllEmploymentTypes()));
    }

    @PostMapping("/employment-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createEmploymentType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employment type created successfully", globalMasterService.createEmploymentType(request)));
    }

    @PutMapping("/employment-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateEmploymentType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employment type updated successfully", globalMasterService.updateEmploymentType(id, request)));
    }

    @DeleteMapping("/employment-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmploymentType(@PathVariable Long id) {
        globalMasterService.deleteEmploymentType(id);
        return ResponseEntity.ok(ApiResponse.success("Employment type deleted successfully", null));
    }

    // ===================== OWNERSHIP TYPES =====================
    @GetMapping("/ownership-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllOwnershipTypes() {
        return ResponseEntity.ok(ApiResponse.success("Ownership types fetched successfully", globalMasterService.getAllOwnershipTypes()));
    }

    @PostMapping("/ownership-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createOwnershipType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ownership type created successfully", globalMasterService.createOwnershipType(request)));
    }

    @PutMapping("/ownership-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateOwnershipType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ownership type updated successfully", globalMasterService.updateOwnershipType(id, request)));
    }

    @DeleteMapping("/ownership-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOwnershipType(@PathVariable Long id) {
        globalMasterService.deleteOwnershipType(id);
        return ResponseEntity.ok(ApiResponse.success("Ownership type deleted successfully", null));
    }

    // ===================== TAXES =====================
    @GetMapping("/taxes")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllTaxes() {
        return ResponseEntity.ok(ApiResponse.success("Taxes fetched successfully", globalMasterService.getAllTaxes()));
    }

    @PostMapping("/taxes")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createTax(@Valid @RequestBody TaxRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tax created successfully", globalMasterService.createTax(request)));
    }

    @PutMapping("/taxes/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateTax(@PathVariable Long id, @Valid @RequestBody TaxRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tax updated successfully", globalMasterService.updateTax(id, request)));
    }

    @DeleteMapping("/taxes/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTax(@PathVariable Long id) {
        globalMasterService.deleteTax(id);
        return ResponseEntity.ok(ApiResponse.success("Tax deleted successfully", null));
    }

    // ===================== DEDUCTION TYPES =====================
    @GetMapping("/deduction-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllDeductionTypes() {
        return ResponseEntity.ok(ApiResponse.success("Deduction types fetched successfully", globalMasterService.getAllDeductionTypes()));
    }

    @PostMapping("/deduction-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createDeductionType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deduction type created successfully", globalMasterService.createDeductionType(request)));
    }

    @PutMapping("/deduction-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateDeductionType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deduction type updated successfully", globalMasterService.updateDeductionType(id, request)));
    }

    @DeleteMapping("/deduction-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDeductionType(@PathVariable Long id) {
        globalMasterService.deleteDeductionType(id);
        return ResponseEntity.ok(ApiResponse.success("Deduction type deleted successfully", null));
    }

    // ===================== PAYMENT STATUSES =====================
    @GetMapping("/payment-statuses")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllPaymentStatuses() {
        return ResponseEntity.ok(ApiResponse.success("Payment statuses fetched successfully", globalMasterService.getAllPaymentStatuses()));
    }

    @PostMapping("/payment-statuses")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createPaymentStatus(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment status created successfully", globalMasterService.createPaymentStatus(request)));
    }

    @PutMapping("/payment-statuses/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updatePaymentStatus(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", globalMasterService.updatePaymentStatus(id, request)));
    }

    @DeleteMapping("/payment-statuses/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentStatus(@PathVariable Long id) {
        globalMasterService.deletePaymentStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Payment status deleted successfully", null));
    }

    // ===================== SERVICE TASK TYPES =====================
    @GetMapping("/service-task-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllServiceTaskTypes() {
        return ResponseEntity.ok(ApiResponse.success("Service task types fetched successfully", globalMasterService.getAllServiceTaskTypes()));
    }

    @PostMapping("/service-task-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createServiceTaskType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service task type created successfully", globalMasterService.createServiceTaskType(request)));
    }

    @PutMapping("/service-task-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateServiceTaskType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service task type updated successfully", globalMasterService.updateServiceTaskType(id, request)));
    }

    @DeleteMapping("/service-task-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteServiceTaskType(@PathVariable Long id) {
        globalMasterService.deleteServiceTaskType(id);
        return ResponseEntity.ok(ApiResponse.success("Service task type deleted successfully", null));
    }

    // ── Equipment Service Task Types ──────────────────────────────────────────

    @GetMapping("/equipment-service-task-types")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getAllEquipmentServiceTaskTypes() {
        return ResponseEntity.ok(ApiResponse.success("Equipment service task types fetched", globalMasterService.getAllEquipmentServiceTaskTypes()));
    }

    @PostMapping("/equipment-service-task-types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> createEquipmentServiceTaskType(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment service task type created", globalMasterService.createEquipmentServiceTaskType(request)));
    }

    @PutMapping("/equipment-service-task-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MasterResponse>> updateEquipmentServiceTaskType(@PathVariable Long id, @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment service task type updated", globalMasterService.updateEquipmentServiceTaskType(id, request)));
    }

    @DeleteMapping("/equipment-service-task-types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEquipmentServiceTaskType(@PathVariable Long id) {
        globalMasterService.deleteEquipmentServiceTaskType(id);
        return ResponseEntity.ok(ApiResponse.success("Equipment service task type deleted", null));
    }

}