package com.feros.api.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private Long tenantId;
    private String phone;
    private String role;
}