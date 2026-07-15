package com.feros.api.dto.response;

import com.feros.api.entity.AppConfig;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfigResponse {
    private Integer minVersion;
    private Integer latestVersion;
    private Boolean forceUpdate;

    public static AppConfigResponse from(AppConfig config) {
        return AppConfigResponse.builder()
                .minVersion(config.getMinVersion())
                .latestVersion(config.getLatestVersion())
                .forceUpdate(config.getForceUpdate())
                .build();
    }
}
