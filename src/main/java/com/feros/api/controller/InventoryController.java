package com.feros.api.controller;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;
import com.feros.api.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ─── Spare Parts Master ───────────────────────────────────────────────────

    @GetMapping("/spare-parts")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STORE_KEEPER', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<SparePartResponse>>> getAllSpareParts() {
        return ResponseEntity.ok(ApiResponse.success("Spare parts fetched", inventoryService.getAllSpareParts()));
    }

    @GetMapping("/spare-parts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STORE_KEEPER', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<SparePartResponse>> getSparePart(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Spare part fetched", inventoryService.getSparePart(id)));
    }

    @PostMapping("/spare-parts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<SparePartResponse>> createSparePart(@Valid @RequestBody SparePartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Spare part created", inventoryService.createSparePart(request)));
    }

    @PutMapping("/spare-parts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<SparePartResponse>> updateSparePart(@PathVariable Long id, @Valid @RequestBody SparePartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Spare part updated", inventoryService.updateSparePart(id, request)));
    }

    @DeleteMapping("/spare-parts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<Void>> deleteSparePart(@PathVariable Long id) {
        inventoryService.deleteSparePart(id);
        return ResponseEntity.ok(ApiResponse.success("Spare part deleted", null));
    }

    @PostMapping("/spare-parts/bulk-upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<BulkTenantUploadResponse>> bulkUploadSpareParts(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Bulk upload completed", inventoryService.bulkUploadSpareParts(file)));
    }

    // ─── Stock ────────────────────────────────────────────────────────────────

    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getStock() {
        return ResponseEntity.ok(ApiResponse.success("Stock fetched", inventoryService.getStock()));
    }

    @PostMapping("/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<Void>> stockIn(@Valid @RequestBody StockInRequest request) {
        inventoryService.stockIn(request);
        return ResponseEntity.ok(ApiResponse.success("Stock added successfully", null));
    }

    @PostMapping("/stock-in/bulk-upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<BulkTenantUploadResponse>> bulkUploadStockIn(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Bulk stock upload completed", inventoryService.bulkUploadStockIn(file)));
    }

    // ─── Transactions ─────────────────────────────────────────────────────────

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<SparePartsTransactionResponse>>> getTransactions() {
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched", inventoryService.getTransactions()));
    }

    @GetMapping("/transactions/part/{sparePartId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<SparePartsTransactionResponse>>> getTransactionsByPart(@PathVariable Long sparePartId) {
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched", inventoryService.getTransactionsByPart(sparePartId)));
    }

    // ─── Service Parts (Request + Approval) ──────────────────────────────────

    @GetMapping("/service-parts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<ServicePartResponse>>> getAllRequests() {
        return ResponseEntity.ok(ApiResponse.success("All requests fetched", inventoryService.getAllRequests()));
    }

    @GetMapping("/service-parts/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<List<ServicePartResponse>>> getPendingRequests() {
        return ResponseEntity.ok(ApiResponse.success("Pending requests fetched", inventoryService.getPendingRequests()));
    }

    @GetMapping("/service-parts/service/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STORE_KEEPER', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<List<ServicePartResponse>>> getPartsForService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success("Service parts fetched", inventoryService.getPartsForService(serviceId)));
    }

    @PostMapping("/service-parts/service/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<ServicePartResponse>> requestPart(@PathVariable Long serviceId, @Valid @RequestBody ServicePartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Part request submitted", inventoryService.requestPart(serviceId, request)));
    }

    @PutMapping("/service-parts/{servicePartId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_KEEPER')")
    public ResponseEntity<ApiResponse<ServicePartResponse>> approvePart(@PathVariable Long servicePartId, @Valid @RequestBody ServicePartApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Part request processed", inventoryService.approvePart(servicePartId, request)));
    }

    @DeleteMapping("/service-parts/{servicePartId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MEN')")
    public ResponseEntity<ApiResponse<Void>> removeServicePart(@PathVariable Long servicePartId) {
        inventoryService.removeServicePart(servicePartId);
        return ResponseEntity.ok(ApiResponse.success("Part request removed", null));
    }
}
