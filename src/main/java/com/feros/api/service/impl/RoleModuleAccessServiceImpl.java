package com.feros.api.service.impl;

import com.feros.api.dto.request.RoleModuleAccessRequest;
import com.feros.api.dto.response.RoleModuleAccessResponse;
import com.feros.api.entity.RoleModuleAccess;
import com.feros.api.entity.Tenant;
import com.feros.api.enums.ModuleKey;
import com.feros.api.enums.RoleName;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.RoleModuleAccessRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.service.RoleModuleAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleModuleAccessServiceImpl implements RoleModuleAccessService {

    private final RoleModuleAccessRepository repository;
    private final TenantRepository tenantRepository;

    /** Modules that are configurable per role. Always-ON items (dashboard, my attendance, etc.) are excluded. */
    private static final Map<RoleName, List<ModuleKey>> ROLE_MODULES = Map.of(
        RoleName.OFFICE_STAFF, List.of(
            ModuleKey.CLIENTS, ModuleKey.ORDERS, ModuleKey.ASSIGNMENTS, ModuleKey.LR_REGISTER,
            ModuleKey.INVOICES, ModuleKey.CREDIT_NOTES, ModuleKey.SERVICE_INVOICES,
            ModuleKey.ATTENDANCE, ModuleKey.REPORTS
        ),
        RoleName.SUPERVISOR, List.of(
            ModuleKey.ORDERS, ModuleKey.ASSIGNMENTS, ModuleKey.LR_REGISTER, ModuleKey.ATTENDANCE
        ),
        RoleName.STORE_KEEPER, List.of(
            ModuleKey.SPARE_PARTS, ModuleKey.TIRES, ModuleKey.PART_REQUESTS, ModuleKey.TIRE_REQUESTS
        ),
        RoleName.SERVICE_MEN, List.of(
            ModuleKey.VEHICLE_SERVICES
        ),
        RoleName.DRIVER, List.of(),
        RoleName.CLEANER, List.of()
    );

    @Override
    public RoleModuleAccessResponse getAll(Long tenantId) {
        // Fetch existing records
        Map<RoleName, Map<ModuleKey, Boolean>> existing = new HashMap<>();
        repository.findByTenantId(tenantId).forEach(r ->
            existing.computeIfAbsent(r.getRole(), k -> new HashMap<>())
                    .put(r.getModuleKey(), r.getEnabled())
        );

        List<RoleModuleAccessResponse.Entry> entries = new ArrayList<>();
        ROLE_MODULES.forEach((role, modules) -> {
            for (ModuleKey mk : modules) {
                boolean enabled = existing.getOrDefault(role, Collections.emptyMap())
                        .getOrDefault(mk, true); // default: enabled (opt-out)
                entries.add(new RoleModuleAccessResponse.Entry(role, mk, enabled));
            }
        });

        return new RoleModuleAccessResponse(entries);
    }

    @Override
    @Transactional
    public void saveAll(Long tenantId, RoleModuleAccessRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        // Delete all existing records for this tenant
        repository.deleteByTenantId(tenantId);

        // Re-insert from request, only for valid role/module combinations
        List<RoleModuleAccess> toSave = new ArrayList<>();
        if (request.getEntries() != null) {
            for (RoleModuleAccessRequest.Entry e : request.getEntries()) {
                List<ModuleKey> allowedModules = ROLE_MODULES.getOrDefault(e.getRole(), List.of());
                if (!allowedModules.contains(e.getModuleKey())) continue; // ignore invalid entries
                toSave.add(RoleModuleAccess.builder()
                        .tenant(tenant)
                        .role(e.getRole())
                        .moduleKey(e.getModuleKey())
                        .enabled(e.getEnabled() != null ? e.getEnabled() : true)
                        .build());
            }
        }
        repository.saveAll(toSave);
    }

    @Override
    public List<String> getEnabledModules(Long tenantId, RoleName role) {
        List<ModuleKey> configurable = ROLE_MODULES.getOrDefault(role, List.of());
        if (configurable.isEmpty()) return List.of(); // DRIVER/CLEANER — no configurable modules

        List<RoleModuleAccess> records = repository.findByTenantIdAndRole(tenantId, role);

        if (records.isEmpty()) {
            // No config exists yet — return all configurable modules (opt-out default)
            return configurable.stream().map(ModuleKey::name).collect(Collectors.toList());
        }

        // Return only enabled ones
        return records.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEnabled()))
                .map(r -> r.getModuleKey().name())
                .collect(Collectors.toList());
    }
}
