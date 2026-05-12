package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkMobilePresentRequest {
    private String selfieUrl;    // optional
    private Double latitude;     // optional
    private Double longitude;    // optional
    private String remarks;      // optional
}
