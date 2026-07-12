package com.feros.api.service;

import com.feros.api.dto.request.EquipmentAdvanceRequest;
import com.feros.api.dto.request.EquipmentPaymentRequest;
import com.feros.api.dto.request.EquipmentRetentionReleaseRequest;
import com.feros.api.dto.response.EquipmentAdvanceResponse;
import com.feros.api.dto.response.EquipmentPaymentResponse;
import com.feros.api.dto.response.EquipmentRetentionReleaseResponse;
import com.feros.api.dto.response.WoReceivablesSummaryResponse;

import java.util.List;

public interface EquipmentReceivablesService {

    // Payments
    EquipmentPaymentResponse recordPayment(Long woId, Long invId, EquipmentPaymentRequest req);
    List<EquipmentPaymentResponse> listPayments(Long woId, Long invId);
    void deletePayment(Long woId, Long invId, Long payId);

    // Advances
    EquipmentAdvanceResponse recordAdvance(Long woId, EquipmentAdvanceRequest req);
    List<EquipmentAdvanceResponse> listAdvances(Long woId);
    void deleteAdvance(Long woId, Long advId);

    // Retention releases
    EquipmentRetentionReleaseResponse recordRetentionRelease(Long woId, EquipmentRetentionReleaseRequest req);
    List<EquipmentRetentionReleaseResponse> listRetentionReleases(Long woId);
    void deleteRetentionRelease(Long woId, Long relId);

    // Reconciliation summary
    WoReceivablesSummaryResponse getSummary(Long woId);
}
