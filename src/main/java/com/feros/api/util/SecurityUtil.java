package com.feros.api.util;

import com.feros.api.config.UserPrincipal;
import com.feros.api.exception.FerosException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
        return getCurrentUser().getTenantId();
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
}