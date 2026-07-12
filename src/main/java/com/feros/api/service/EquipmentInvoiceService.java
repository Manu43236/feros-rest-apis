package com.feros.api.service;

import com.feros.api.dto.request.EquipmentInvoiceRequest;
import com.feros.api.dto.response.EquipmentInvoiceCalcResult;
import com.feros.api.dto.response.EquipmentInvoicePrefillResponse;
import com.feros.api.dto.response.EquipmentInvoiceResponse;
import com.feros.api.enums.EquipmentInvoiceStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface EquipmentInvoiceService {

    EquipmentInvoiceResponse create(Long woId, EquipmentInvoiceRequest request);
    EquipmentInvoiceResponse createForClient(EquipmentInvoiceRequest request);

    List<EquipmentInvoiceResponse> getByWorkOrder(Long woId);

    Page<EquipmentInvoiceResponse> getAll(int page, int size, EquipmentInvoiceStatus status, Long clientId);

    EquipmentInvoiceResponse getById(Long id);

    EquipmentInvoiceResponse update(Long id, EquipmentInvoiceRequest request);

    EquipmentInvoiceResponse updateStatus(Long id, EquipmentInvoiceStatus newStatus);

    void delete(Long id);

    List<EquipmentInvoicePrefillResponse> prefill(Long woId, LocalDate from, LocalDate to);
    List<EquipmentInvoicePrefillResponse> prefillByClient(Long clientId, LocalDate from, LocalDate to);

    List<EquipmentInvoiceCalcResult> calculate(Long woId, LocalDate from, LocalDate to);
}
