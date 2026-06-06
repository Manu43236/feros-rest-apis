package com.feros.api.gps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TataAuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    /** Token lifetime in MINUTES (TATA Fleet Edge uses minutes, not seconds) */
    @JsonProperty("expires_in")
    private Integer expiresInMinutes;
}
