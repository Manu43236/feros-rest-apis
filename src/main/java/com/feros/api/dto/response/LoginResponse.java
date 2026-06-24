package com.feros.api.dto.response;

import com.feros.api.enums.ModuleType;
import lombok.*;

import java.util.List;

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
    /** Null for ADMIN/SUPER_ADMIN (all modules visible). Non-null list of enabled module keys for other roles. */
    private List<String> allowedModules;
    private ModuleType moduleType;
}
