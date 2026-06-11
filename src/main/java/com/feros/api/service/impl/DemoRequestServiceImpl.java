package com.feros.api.service.impl;

import com.feros.api.dto.request.DemoRequestCreateDto;
import com.feros.api.dto.response.DemoRequestResponse;
import com.feros.api.entity.DemoRequest;
import com.feros.api.enums.DemoRequestStatus;
import com.feros.api.exception.FerosException;
import org.springframework.http.HttpStatus;
import com.feros.api.repository.DemoRequestRepository;
import com.feros.api.service.DemoRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoRequestServiceImpl implements DemoRequestService {

    private final DemoRequestRepository demoRequestRepository;

    @Override
    @Transactional
    public void create(DemoRequestCreateDto dto) {
        DemoRequest entity = DemoRequest.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .company(dto.getCompany())
                .email(dto.getEmail())
                .fleetSize(dto.getFleetSize())
                .city(dto.getCity())
                .status(DemoRequestStatus.NEW)
                .build();
        demoRequestRepository.save(entity);
    }

    @Override
    public Page<DemoRequestResponse> getAll(DemoRequestStatus status, Pageable pageable) {
        Page<DemoRequest> page = status != null
                ? demoRequestRepository.findByStatus(status, pageable)
                : demoRequestRepository.findAll(pageable);
        return page.map(DemoRequestResponse::from);
    }

    @Override
    @Transactional
    public DemoRequestResponse updateStatus(Long id, DemoRequestStatus status, String notes) {
        DemoRequest entity = demoRequestRepository.findById(id)
                .orElseThrow(() -> new FerosException("Demo request not found: " + id, HttpStatus.NOT_FOUND));
        entity.setStatus(status);
        if (notes != null) entity.setNotes(notes);
        return DemoRequestResponse.from(demoRequestRepository.save(entity));
    }

    @Override
    public long countNew() {
        return demoRequestRepository.countByStatus(DemoRequestStatus.NEW);
    }
}
