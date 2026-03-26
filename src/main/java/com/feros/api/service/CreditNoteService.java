package com.feros.api.service;

import com.feros.api.dto.request.CreditNoteRequest;
import com.feros.api.dto.response.CreditNoteResponse;
import com.feros.api.enums.CreditNoteStatus;

import java.util.List;

public interface CreditNoteService {
    CreditNoteResponse create(CreditNoteRequest request);
    List<CreditNoteResponse> getAll();
    List<CreditNoteResponse> getByClient(Long clientId);
    CreditNoteResponse getById(Long id);
    CreditNoteResponse updateStatus(Long id, CreditNoteStatus status);
    void delete(Long id);
}
