package com.feros.api.service;

import com.feros.api.dto.request.TripExpenseApproveRequest;
import com.feros.api.dto.request.TripExpenseRejectRequest;
import com.feros.api.dto.request.TripExpenseRequest;
import com.feros.api.dto.request.TripExpenseSettleRequest;
import com.feros.api.dto.response.TripExpenseResponse;
import com.feros.api.enums.TripExpenseStatus;

import java.util.List;

public interface TripExpenseService {
    TripExpenseResponse createDraft(Long lrId, TripExpenseRequest request);
    TripExpenseResponse updateDraft(Long lrId, TripExpenseRequest request);
    TripExpenseResponse submit(Long lrId);
    TripExpenseResponse getByLrId(Long lrId);
    List<TripExpenseResponse> getAll(TripExpenseStatus status);
    TripExpenseResponse approve(Long id, TripExpenseApproveRequest request);
    TripExpenseResponse reject(Long id, TripExpenseRejectRequest request);
    TripExpenseResponse settle(Long id, TripExpenseSettleRequest request);
    void deleteDraft(Long lrId);
}
