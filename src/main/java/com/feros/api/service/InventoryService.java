package com.feros.api.service;

import com.feros.api.dto.request.*;
import com.feros.api.dto.response.*;

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

    // Service Parts (request + approval)
    ServicePartResponse requestPart(Long serviceId, ServicePartRequest request);
    ServicePartResponse approvePart(Long servicePartId, ServicePartApprovalRequest request);
    void removeServicePart(Long servicePartId);
    List<ServicePartResponse> getPartsForService(Long serviceId);
    List<ServicePartResponse> getPendingRequests();

    // Transactions
    List<SparePartsTransactionResponse> getTransactions();
    List<SparePartsTransactionResponse> getTransactionsByPart(Long sparePartId);
}
