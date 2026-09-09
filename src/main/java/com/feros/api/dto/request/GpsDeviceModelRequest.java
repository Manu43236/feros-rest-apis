package com.feros.api.dto.request;

import com.feros.api.enums.GpsConnectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GpsDeviceModelRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Model name is required")
    private String modelName;

    @NotNull(message = "Connection type is required")
    private GpsConnectionType connectionType;

    @NotBlank(message = "Parser key is required")
    private String parserKey;

    private String protocolVersion;
    private String description;
}
