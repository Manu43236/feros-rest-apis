package com.feros.api.service.impl;

import com.feros.api.dto.request.EquipmentAttachmentRequest;
import com.feros.api.dto.response.EquipmentAttachmentResponse;
import com.feros.api.entity.EquipmentAttachment;
import com.feros.api.entity.Tenant;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.EquipmentAttachmentRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.service.EquipmentAttachmentService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentAttachmentServiceImpl implements EquipmentAttachmentService {

    private final EquipmentAttachmentRepository attachmentRepository;
    private final TenantRepository tenantRepository;

    private Long tenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getTenant(Long id) {
        return tenantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    private EquipmentAttachment find(Long id) {
        return attachmentRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new FerosException("Attachment not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentAttachmentResponse> getAll() {
        return attachmentRepository.findByTenantIdOrderByNameAsc(tenantId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentAttachmentResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public EquipmentAttachmentResponse create(EquipmentAttachmentRequest req) {
        EquipmentAttachment attachment = EquipmentAttachment.builder()
                .tenant(getTenant(tenantId()))
                .name(req.getName())
                .type(req.getType())
                .serialNumber(req.getSerialNumber())
                .ownershipType(req.getOwnershipType())
                .hiredFrom(req.getHiredFrom())
                .hireStartDate(req.getHireStartDate())
                .hireEndDate(req.getHireEndDate())
                .defaultRate(req.getDefaultRate())
                .rateUnit(req.getRateUnit())
                .notes(req.getNotes())
                .isActive(true)
                .build();
        return toResponse(attachmentRepository.save(attachment));
    }

    @Override
    @Transactional
    public EquipmentAttachmentResponse update(Long id, EquipmentAttachmentRequest req) {
        EquipmentAttachment attachment = find(id);
        attachment.setName(req.getName());
        attachment.setType(req.getType());
        attachment.setSerialNumber(req.getSerialNumber());
        attachment.setOwnershipType(req.getOwnershipType());
        attachment.setHiredFrom(req.getHiredFrom());
        attachment.setHireStartDate(req.getHireStartDate());
        attachment.setHireEndDate(req.getHireEndDate());
        attachment.setDefaultRate(req.getDefaultRate());
        attachment.setRateUnit(req.getRateUnit());
        attachment.setNotes(req.getNotes());
        return toResponse(attachmentRepository.save(attachment));
    }

    @Override
    @Transactional
    public EquipmentAttachmentResponse setActive(Long id, boolean active) {
        EquipmentAttachment attachment = find(id);
        attachment.setIsActive(active);
        return toResponse(attachmentRepository.save(attachment));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        attachmentRepository.delete(find(id));
    }

    private EquipmentAttachmentResponse toResponse(EquipmentAttachment a) {
        return EquipmentAttachmentResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .type(a.getType())
                .serialNumber(a.getSerialNumber())
                .ownershipType(a.getOwnershipType())
                .hiredFrom(a.getHiredFrom())
                .hireStartDate(a.getHireStartDate())
                .hireEndDate(a.getHireEndDate())
                .defaultRate(a.getDefaultRate())
                .rateUnit(a.getRateUnit())
                .notes(a.getNotes())
                .isActive(a.getIsActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
