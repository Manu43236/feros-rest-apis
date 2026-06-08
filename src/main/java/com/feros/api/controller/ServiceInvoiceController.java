package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.ServiceInvoiceResponse;
import com.feros.api.service.ServiceInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/service-invoices")
@RequiredArgsConstructor
public class ServiceInvoiceController {

    private final ServiceInvoiceService serviceInvoiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<List<ServiceInvoiceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Service invoices fetched", serviceInvoiceService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<ServiceInvoiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched", serviceInvoiceService.getById(id)));
    }

    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SERVICE_MANAGER')")
    public ResponseEntity<ApiResponse<ServiceInvoiceResponse>> getByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched", serviceInvoiceService.getByServiceId(serviceId)));
    }

    @PutMapping("/{id}/vendor-amount")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ServiceInvoiceResponse>> updateVendorAmount(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        BigDecimal amount = body.get("vendorAmount") != null
                ? new BigDecimal(body.get("vendorAmount").toString()) : null;
        String invoiceNo = body.get("vendorInvoiceNo") != null
                ? body.get("vendorInvoiceNo").toString() : null;
        return ResponseEntity.ok(ApiResponse.success("Vendor amount updated",
                serviceInvoiceService.updateVendorAmount(id, amount, invoiceNo)));
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF')")
    public ResponseEntity<ApiResponse<ServiceInvoiceResponse>> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid", serviceInvoiceService.markPaid(id)));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF','SERVICE_MANAGER')")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        byte[] pdf = serviceInvoiceService.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"service-invoice-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
