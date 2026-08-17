package com.feros.api.config;

import com.feros.api.entity.UserSession;
import com.feros.api.repository.UserSessionRepository;
import com.feros.api.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 1. Validate JWT signature + expiry — explicit 401 so mobile redirects to login
        if (!jwtUtil.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"TOKEN_EXPIRED\"}");
            return;
        }

        // 2. Impersonation tokens are JWT-only — skip DB session check
        if (!jwtUtil.isImpersonationToken(token)) {
            Optional<UserSession> sessionOpt = userSessionRepository.findByToken(token);
            if (sessionOpt.isEmpty()) {
                // Token was displaced by a new login on another device
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"SESSION_DISPLACED\"}");
                return;
            }
            // 3. Update last active timestamp
            UserSession session = sessionOpt.get();
            session.setLastActiveAt(LocalDateTime.now());
            userSessionRepository.save(session);
        }

        // 4. Set authentication in security context
        String phone = jwtUtil.extractPhone(token);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
        Long tenantId = jwtUtil.extractTenantId(token);

        UserPrincipal userPrincipal = new UserPrincipal(userId, tenantId, phone, role);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
