package com.feros.api.service;

import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.ConfirmSubscriptionPaymentRequest;
import com.feros.api.dto.request.CorrectSubscriptionRequest;
import com.feros.api.dto.request.CreateProformaInvoiceRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;
import com.feros.api.dto.response.SubscriptionInvoiceSummaryResponse;

import java.util.List;

public interface SubscriptionService {
    SubscriptionHistoryResponse activate(Long tenantId, ActivateSubscriptionRequest request);
    SubscriptionHistoryResponse extendTrial(Long tenantId, ExtendSubscriptionRequest request);
    SubscriptionHistoryResponse extendSubscription(Long tenantId, ExtendSubscriptionRequest request);
    SubscriptionHistoryResponse suspend(Long tenantId, SuspendSubscriptionRequest request);
    SubscriptionHistoryResponse reactivate(Long tenantId, String notes);
    List<SubscriptionHistoryResponse> getHistory(Long tenantId);
    List<SubscriptionInvoiceResponse> getInvoices(Long tenantId);
    SubscriptionInvoiceResponse getInvoiceById(Long tenantId, Long invoiceId);
    byte[] generateInvoicePdf(Long tenantId, Long invoiceId);
    SubscriptionHistoryResponse getCurrentSubscription(Long tenantId);
    SubscriptionHistoryResponse correctSubscription(Long tenantId, CorrectSubscriptionRequest request);
    SubscriptionInvoiceResponse generateInvoiceForHistory(Long tenantId, Long historyId);
    SubscriptionInvoiceResponse createProformaInvoice(Long tenantId, CreateProformaInvoiceRequest request);
    SubscriptionInvoiceResponse confirmPayment(Long tenantId, Long invoiceId, ConfirmSubscriptionPaymentRequest request);
    SubscriptionInvoiceSummaryResponse getInvoiceSummary(Long tenantId);
    void deleteProformaInvoice(Long tenantId, Long invoiceId);
    List<SubscriptionInvoiceResponse> getAllInvoices(Long tenantId, Integer year, Integer month);
}
