package com.feros.api.service;

import com.feros.api.dto.request.TyreRequestApproveRequest;
import com.feros.api.dto.request.TyreRequestCreateRequest;
import com.feros.api.dto.request.TyreRequestRejectRequest;
import com.feros.api.dto.response.TyreRequestResponse;

import java.util.List;

public interface TyreRequestService {
    TyreRequestResponse createRequest(TyreRequestCreateRequest request);
    List<TyreRequestResponse> getAll();
    List<TyreRequestResponse> getPending();
    List<TyreRequestResponse> getMyRequests();
    TyreRequestResponse approveRequest(Long id, TyreRequestApproveRequest request);
    TyreRequestResponse rejectRequest(Long id, TyreRequestRejectRequest request);
}
