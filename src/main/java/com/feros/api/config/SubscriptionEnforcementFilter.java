package com.feros.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feros.api.enums.SubscriptionStatus;
import com.feros.api.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SubscriptionEnforcementFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Skip if unauthenticated (handled by JWT filter) or SUPER_ADMIN
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("");

        if ("ROLE_SUPER_ADMIN".equals(role)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Always allow GET requests (read-only access) and auth/subscription endpoints
        String method  = request.getMethod();
        String path    = request.getRequestURI();
        if ("GET".equalsIgnoreCase(method)
                || path.contains("/auth/")
                || path.contains("/subscriptions/my")
                || path.contains("/subscriptions/upgrade-request")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract tenantId from the principal
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal) || userPrincipal.getTenantId() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long tenantId = userPrincipal.getTenantId();

        SubscriptionStatus status = tenantRepository.findById(tenantId)
                .map(t -> t.getSubscriptionStatus())
                .orElse(null);

        if (status == SubscriptionStatus.EXPIRED) {
            writeError(response, HttpServletResponse.SC_PAYMENT_REQUIRED,
                    "Your subscription has expired. You can view your data but cannot make changes. Please upgrade to continue.");
            return;
        }

        if (status == SubscriptionStatus.SUSPENDED) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Your account has been suspended. Please contact FEROS support.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                Map.of("success", false, "message", message, "data", null));
    }
}
