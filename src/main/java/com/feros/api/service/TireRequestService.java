package com.feros.api.service;

import com.feros.api.dto.request.TireRequestApproveRequest;
import com.feros.api.dto.request.TireRequestCreateRequest;
import com.feros.api.dto.request.TireRequestRejectRequest;
import com.feros.api.dto.response.TireRequestResponse;

import java.util.List;

public interface TireRequestService {
    TireRequestResponse createRequest(TireRequestCreateRequest request);
    List<TireRequestResponse> getAll();
    List<TireRequestResponse> getPending();
    List<TireRequestResponse> getMyRequests();
    TireRequestResponse approveRequest(Long id, TireRequestApproveRequest request);
    TireRequestResponse rejectRequest(Long id, TireRequestRejectRequest request);
}
