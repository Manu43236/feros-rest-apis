package com.feros.api.controller;

import com.feros.api.dto.request.EquipmentMakeRequest;
import com.feros.api.dto.request.EquipmentModelRequest;
import com.feros.api.dto.request.EquipmentTypeRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.EquipmentMakeResponse;
import com.feros.api.dto.response.EquipmentModelResponse;
import com.feros.api.dto.response.EquipmentTypeResponse;
import com.feros.api.service.EquipmentMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment/masters")
@RequiredArgsConstructor
public class EquipmentMasterController {

    private final EquipmentMasterService equipmentMasterService;

    // ===================== MAKES =====================

    @GetMapping("/makes")
    public ResponseEntity<ApiResponse<List<EquipmentMakeResponse>>> getAllMakes() {
        return ResponseEntity.ok(ApiResponse.success("Equipment makes fetched successfully",
                equipmentMasterService.getAllMakes()));
    }

    @GetMapping("/makes/{id}")
    public ResponseEntity<ApiResponse<EquipmentMakeResponse>> getMakeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Equipment make fetched successfully",
                equipmentMasterService.getMakeById(id)));
    }

    @PostMapping("/makes")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentMakeResponse>> createMake(@Valid @RequestBody EquipmentMakeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment make created successfully",
                equipmentMasterService.createMake(request)));
    }

    @PutMapping("/makes/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentMakeResponse>> updateMake(@PathVariable Long id,
            @Valid @RequestBody EquipmentMakeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment make updated successfully",
                equipmentMasterService.updateMake(id, request)));
    }

    @DeleteMapping("/makes/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMake(@PathVariable Long id) {
        equipmentMasterService.deleteMake(id);
        return ResponseEntity.ok(ApiResponse.success("Equipment make deleted successfully", null));
    }

    // ===================== MODELS =====================

    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<EquipmentModelResponse>>> getAllModels(
            @RequestParam(required = false) Long makeId) {
        return ResponseEntity.ok(ApiResponse.success("Equipment models fetched successfully",
                equipmentMasterService.getAllModels(makeId)));
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<ApiResponse<EquipmentModelResponse>> getModelById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Equipment model fetched successfully",
                equipmentMasterService.getModelById(id)));
    }

    @PostMapping("/models")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentModelResponse>> createModel(@Valid @RequestBody EquipmentModelRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment model created successfully",
                equipmentMasterService.createModel(request)));
    }

    @PutMapping("/models/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentModelResponse>> updateModel(@PathVariable Long id,
            @Valid @RequestBody EquipmentModelRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment model updated successfully",
                equipmentMasterService.updateModel(id, request)));
    }

    @DeleteMapping("/models/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteModel(@PathVariable Long id) {
        equipmentMasterService.deleteModel(id);
        return ResponseEntity.ok(ApiResponse.success("Equipment model deleted successfully", null));
    }

    // ===================== TYPES =====================

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<EquipmentTypeResponse>>> getAllTypes(
            @RequestParam(required = false) Long modelId) {
        return ResponseEntity.ok(ApiResponse.success("Equipment types fetched successfully",
                equipmentMasterService.getAllTypes(modelId)));
    }

    @GetMapping("/types/{id}")
    public ResponseEntity<ApiResponse<EquipmentTypeResponse>> getTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Equipment type fetched successfully",
                equipmentMasterService.getTypeById(id)));
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentTypeResponse>> createType(@Valid @RequestBody EquipmentTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment type created successfully",
                equipmentMasterService.createType(request)));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentTypeResponse>> updateType(@PathVariable Long id,
            @Valid @RequestBody EquipmentTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment type updated successfully",
                equipmentMasterService.updateType(id, request)));
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteType(@PathVariable Long id) {
        equipmentMasterService.deleteType(id);
        return ResponseEntity.ok(ApiResponse.success("Equipment type deleted successfully", null));
    }
}
