package com.feros.api.service;

import com.feros.api.dto.request.DemoRequestCreateDto;
import com.feros.api.dto.response.DemoRequestResponse;
import com.feros.api.enums.DemoRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DemoRequestService {
    void create(DemoRequestCreateDto dto);
    Page<DemoRequestResponse> getAll(DemoRequestStatus status, Pageable pageable);
    DemoRequestResponse updateStatus(Long id, DemoRequestStatus status, String notes);
    long countNew();
}
