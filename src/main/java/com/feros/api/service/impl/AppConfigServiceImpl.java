package com.feros.api.service.impl;

import com.feros.api.dto.request.AppConfigRequest;
import com.feros.api.dto.response.AppConfigResponse;
import com.feros.api.entity.AppConfig;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.AppConfigRepository;
import com.feros.api.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppConfigServiceImpl implements AppConfigService {

    private final AppConfigRepository appConfigRepository;

    @Override
    public AppConfigResponse get() {
        return appConfigRepository.findAll().stream()
                .findFirst()
                .map(AppConfigResponse::from)
                .orElseThrow(() -> new FerosException("App config not initialized", HttpStatus.NOT_FOUND));
    }

    @Override
    public AppConfigResponse update(AppConfigRequest request) {
        AppConfig config = appConfigRepository.findAll().stream()
                .findFirst()
                .orElse(AppConfig.builder().build());

        config.setMinVersion(request.getMinVersion());
        config.setLatestVersion(request.getLatestVersion());
        config.setForceUpdate(request.getForceUpdate());

        return AppConfigResponse.from(appConfigRepository.save(config));
    }
}
