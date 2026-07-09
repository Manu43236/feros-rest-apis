package com.feros.api.service;

import com.feros.api.dto.request.LeaseInvoiceRequest;
import com.feros.api.dto.response.LeaseInvoicePrefillResponse;
import com.feros.api.dto.response.LeaseInvoiceResponse;
import com.feros.api.enums.EquipmentInvoiceStatus;

import java.time.LocalDate;
import java.util.List;

public interface LeaseInvoiceService {
    List<LeaseInvoiceResponse> getAll();
    List<LeaseInvoicePrefillResponse> prefill(Long leaseId, LocalDate from, LocalDate to);
    LeaseInvoiceResponse create(Long leaseId, LeaseInvoiceRequest request);
    List<LeaseInvoiceResponse> getByLease(Long leaseId);
    LeaseInvoiceResponse getById(Long id);
    LeaseInvoiceResponse updateStatus(Long id, EquipmentInvoiceStatus status);
    void delete(Long id);
}
