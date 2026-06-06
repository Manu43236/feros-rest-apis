package com.feros.api.dto.response;

import com.feros.api.entity.GpsProviderConfig;
import com.feros.api.enums.GpsProviderType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GpsProviderConfigResponse {

    private Long id;
    private GpsProviderType providerType;
    private String displayName;
    private String apiBaseUrl;
    private Boolean isActive;
    private LocalDateTime lastSyncAt;
    private String syncStatus;
    private String syncErrorMsg;
    private LocalDateTime createdAt;

    public static GpsProviderConfigResponse from(GpsProviderConfig config) {
        return GpsProviderConfigResponse.builder()
                .id(config.getId())
                .providerType(config.getProviderType())
                .displayName(config.getDisplayName())
                .apiBaseUrl(config.getApiBaseUrl())
                .isActive(config.getIsActive())
                .lastSyncAt(config.getLastSyncAt())
                .syncStatus(config.getSyncStatus())
                .syncErrorMsg(config.getSyncErrorMsg())
                .createdAt(config.getCreatedAt())
                .build();
    }
}
