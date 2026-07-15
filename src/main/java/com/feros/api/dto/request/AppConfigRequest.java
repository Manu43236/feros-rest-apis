package com.feros.api.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigRequest {
    private Integer minVersion;
    private Integer latestVersion;
    private Boolean forceUpdate;
}
