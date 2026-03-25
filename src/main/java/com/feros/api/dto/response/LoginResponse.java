package com.feros.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private String phone;
    private String role;
    private Long tenantId;
    private String companyName;
    private String logoUrl;
    private boolean isPinResetRequired;
}