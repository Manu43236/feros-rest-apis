package com.feros.api.service;

import com.feros.api.dto.request.CreateInvoiceRequest;
import com.feros.api.dto.request.InvoicePaymentRequest;
import com.feros.api.dto.request.UpdateInvoiceRequest;
import com.feros.api.dto.request.UpdateInvoiceStatusRequest;
import com.feros.api.dto.response.InvoicePaymentResponse;
import com.feros.api.dto.response.InvoiceResponse;
import com.feros.api.enums.InvoiceStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InvoiceService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    InvoiceResponse getInvoiceById(Long id);
    Page<InvoiceResponse> getAllInvoices(int page, int size, String search, InvoiceStatus status);
    List<InvoiceResponse> getInvoicesByClient(Long clientId);
    InvoiceResponse updateInvoiceStatus(Long id, UpdateInvoiceStatusRequest request);
    InvoiceResponse updateInvoice(Long id, UpdateInvoiceRequest request);
    InvoicePaymentResponse recordPayment(Long id, InvoicePaymentRequest request);
    List<InvoicePaymentResponse> getPayments(Long id);
    void deletePayment(Long invoiceId, Long paymentId);
    List<Long> getInvoicedLrIds();
    List<InvoiceResponse> getInvoicesByOrder(Long orderId);
}