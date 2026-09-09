package com.feros.api.service.impl;

import com.feros.api.dto.request.GpsDeviceModelRequest;
import com.feros.api.dto.response.GpsDeviceModelResponse;
import com.feros.api.entity.master.GpsDeviceModel;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.GpsDeviceModelRepository;
import com.feros.api.service.GpsHardwareCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GpsHardwareCatalogServiceImpl implements GpsHardwareCatalogService {

    private final GpsDeviceModelRepository repo;

    @Override
    public List<GpsDeviceModelResponse> getAll(String companyName) {
        List<GpsDeviceModel> models = (companyName != null && !companyName.isBlank())
                ? repo.findAllByCompanyNameIgnoreCaseOrderByModelNameAsc(companyName)
                : repo.findAllByOrderByCompanyNameAscModelNameAsc();
        return models.stream().map(this::toResponse).toList();
    }

    @Override
    public List<GpsDeviceModelResponse> getAllActive() {
        return repo.findAllByIsActiveTrueOrderByCompanyNameAscModelNameAsc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    public GpsDeviceModelResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Override
    public GpsDeviceModelResponse create(GpsDeviceModelRequest request) {
        if (repo.findByParserKey(request.getParserKey()).isPresent()) {
            throw new FerosException("Parser key '" + request.getParserKey() + "' already exists", HttpStatus.CONFLICT);
        }
        GpsDeviceModel model = GpsDeviceModel.builder()
                .companyName(request.getCompanyName().trim())
                .modelName(request.getModelName().trim())
                .connectionType(request.getConnectionType())
                .parserKey(request.getParserKey().trim().toUpperCase())
                .protocolVersion(request.getProtocolVersion())
                .description(request.getDescription())
                .isActive(true)
                .build();
        return toResponse(repo.save(model));
    }

    @Override
    public GpsDeviceModelResponse update(Long id, GpsDeviceModelRequest request) {
        GpsDeviceModel model = find(id);
        repo.findByParserKey(request.getParserKey().trim().toUpperCase())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(__ -> { throw new FerosException("Parser key '" + request.getParserKey() + "' already exists", HttpStatus.CONFLICT); });

        model.setCompanyName(request.getCompanyName().trim());
        model.setModelName(request.getModelName().trim());
        model.setConnectionType(request.getConnectionType());
        model.setParserKey(request.getParserKey().trim().toUpperCase());
        model.setProtocolVersion(request.getProtocolVersion());
        model.setDescription(request.getDescription());
        return toResponse(repo.save(model));
    }

    @Override
    public GpsDeviceModelResponse toggleActive(Long id) {
        GpsDeviceModel model = find(id);
        model.setIsActive(!model.getIsActive());
        return toResponse(repo.save(model));
    }

    private GpsDeviceModel find(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new FerosException("GPS device model not found", HttpStatus.NOT_FOUND));
    }

    private GpsDeviceModelResponse toResponse(GpsDeviceModel m) {
        return GpsDeviceModelResponse.builder()
                .id(m.getId())
                .companyName(m.getCompanyName())
                .modelName(m.getModelName())
                .connectionType(m.getConnectionType())
                .parserKey(m.getParserKey())
                .protocolVersion(m.getProtocolVersion())
                .description(m.getDescription())
                .isActive(m.getIsActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
