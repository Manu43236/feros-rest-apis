package com.feros.api.service;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InventoryService {

    // Spare Parts Master
    SparePartResponse createSparePart(SparePartRequest request);
    SparePartResponse updateSparePart(Long id, SparePartRequest request);
    SparePartResponse getSparePart(Long id);
    List<SparePartResponse> getAllSpareParts();
    void deleteSparePart(Long id);

    // Stock
    List<StockItemResponse> getStock();
    void stockIn(StockInRequest request);
    void stockOut(StockOutRequest request);

    // Service Parts (request + approval)
    ServicePartResponse requestPart(Long serviceId, ServicePartRequest request);
    ServicePartResponse approvePart(Long servicePartId, ServicePartApprovalRequest request);
    void removeServicePart(Long servicePartId);
    List<ServicePartResponse> getPartsForService(Long serviceId);
    List<ServicePartResponse> getPendingRequests();
    List<ServicePartResponse> getAllRequests();

    // Transactions
    List<SparePartsTransactionResponse> getTransactions();
    List<SparePartsTransactionResponse> getTransactionsByPart(Long sparePartId);

    // Bulk uploads
    BulkTenantUploadResponse bulkUploadSpareParts(MultipartFile file);
    BulkTenantUploadResponse bulkUploadStockIn(MultipartFile file);
}
