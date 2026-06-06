package com.feros.api.dto.request;

import com.feros.api.enums.GpsProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GpsProviderConfigRequest {

    @NotNull
    private GpsProviderType providerType;

    private String displayName;

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    private String apiBaseUrl;
}
