package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BroadcastNotificationRequest {
    private Long tenantId; // null = broadcast to all tenants
    @NotBlank private String title;
    @NotBlank private String message;
}
