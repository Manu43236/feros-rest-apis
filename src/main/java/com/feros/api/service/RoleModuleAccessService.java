package com.feros.api.service;

import com.feros.api.dto.request.RoleModuleAccessRequest;
import com.feros.api.dto.response.RoleModuleAccessResponse;
import com.feros.api.enums.RoleName;

import java.util.List;

public interface RoleModuleAccessService {

    /** Returns full config for all roles (for settings page — ADMIN only) */
    RoleModuleAccessResponse getAll(Long tenantId);

    /** Saves bulk config (replaces existing) */
    void saveAll(Long tenantId, RoleModuleAccessRequest request);

    /** Returns list of enabled module keys for a specific role at login time */
    List<String> getEnabledModules(Long tenantId, RoleName role);
}
