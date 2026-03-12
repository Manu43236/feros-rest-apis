package com.feros.api.util;

import com.feros.api.config.UserPrincipal;
import com.feros.api.exception.FerosException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SecurityUtil {

    public static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new FerosException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        return authentication;
    }

    public static UserPrincipal getCurrentUser() {
        Object principal = getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        throw new FerosException("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public static Long getCurrentTenantId() {
        UserPrincipal user = getCurrentUser();

        // If SUPER_ADMIN, check for X-Tenant-Id header
        if ("SUPER_ADMIN".equals(user.getRole())) {
            String tenantIdHeader = getRequestHeader("X-Tenant-Id");
            if (tenantIdHeader != null && !tenantIdHeader.isBlank()) {
                return Long.valueOf(tenantIdHeader);
            }
            throw new FerosException(
                    "SUPER_ADMIN must provide X-Tenant-Id header", HttpStatus.BAD_REQUEST);
        }

        // For ADMIN/OFFICE_STAFF — use tenantId from JWT
        Long tenantId = user.getTenantId();
        if (tenantId == null) {
            throw new FerosException("Tenant not found in token", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    public static String getCurrentRole() {
        return getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new FerosException(
                        "No role found", HttpStatus.UNAUTHORIZED))
                .replace("ROLE_", "");
    }

    public static boolean isSuperAdmin() {
        return getCurrentRole().equals("SUPER_ADMIN");
    }

    public static boolean isAdmin() {
        return getCurrentRole().equals("ADMIN");
    }

    private static String getRequestHeader(String headerName) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader(headerName);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
