package com.feros.api.service;

import com.feros.api.dto.request.ClientAdvanceRequest;
import com.feros.api.dto.response.ClientAdvanceResponse;

import java.util.List;

public interface ClientAdvanceService {
    ClientAdvanceResponse create(ClientAdvanceRequest request);
    List<ClientAdvanceResponse> getAll();
    List<ClientAdvanceResponse> getByClient(Long clientId);
    ClientAdvanceResponse getById(Long id);
    void delete(Long id);
}
