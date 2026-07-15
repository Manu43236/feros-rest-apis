package com.feros.api.service;

import com.feros.api.dto.request.AppConfigRequest;
import com.feros.api.dto.response.AppConfigResponse;

public interface AppConfigService {
    AppConfigResponse get();
    AppConfigResponse update(AppConfigRequest request);
}
