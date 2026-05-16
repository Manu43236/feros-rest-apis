package com.feros.api.service;

import com.feros.api.dto.response.ServiceInvoiceResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ServiceInvoiceService {
    ServiceInvoiceResponse getByServiceId(Long serviceId);
    ServiceInvoiceResponse getById(Long invoiceId);
    List<ServiceInvoiceResponse> getAll();
    ServiceInvoiceResponse updateVendorAmount(Long invoiceId, BigDecimal vendorAmount, String vendorInvoiceNo);
    ServiceInvoiceResponse markPaid(Long invoiceId);
    byte[] generatePdf(Long invoiceId);
}
