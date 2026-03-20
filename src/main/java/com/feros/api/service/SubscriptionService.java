package com.feros.api.service;

import com.feros.api.dto.request.ActivateSubscriptionRequest;
import com.feros.api.dto.request.ExtendSubscriptionRequest;
import com.feros.api.dto.request.SuspendSubscriptionRequest;
import com.feros.api.dto.response.SubscriptionHistoryResponse;
import com.feros.api.dto.response.SubscriptionInvoiceResponse;

import java.util.List;

public interface SubscriptionService {
    SubscriptionHistoryResponse activate(Long tenantId, ActivateSubscriptionRequest request);
    SubscriptionHistoryResponse extendTrial(Long tenantId, ExtendSubscriptionRequest request);
    SubscriptionHistoryResponse extendSubscription(Long tenantId, ExtendSubscriptionRequest request);
    SubscriptionHistoryResponse suspend(Long tenantId, SuspendSubscriptionRequest request);
    SubscriptionHistoryResponse reactivate(Long tenantId, String notes);
    List<SubscriptionHistoryResponse> getHistory(Long tenantId);
    List<SubscriptionInvoiceResponse> getInvoices(Long tenantId);
    SubscriptionHistoryResponse getCurrentSubscription(Long tenantId);
}
