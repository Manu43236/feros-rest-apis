package com.feros.api.service;

import com.feros.api.dto.request.ConsumableRequestCreateRequest;
import com.feros.api.dto.request.ConsumableRequestIssueRequest;
import com.feros.api.dto.request.ConsumableRequestRejectRequest;
import com.feros.api.dto.response.ConsumableRequestResponse;

import java.util.List;

public interface ConsumableRequestService {
    ConsumableRequestResponse createRequest(ConsumableRequestCreateRequest request);
    List<ConsumableRequestResponse> getAll();
    List<ConsumableRequestResponse> getPending();
    List<ConsumableRequestResponse> getMyRequests();
    ConsumableRequestResponse issueRequest(Long id, ConsumableRequestIssueRequest request);
    ConsumableRequestResponse rejectRequest(Long id, ConsumableRequestRejectRequest request);
}
