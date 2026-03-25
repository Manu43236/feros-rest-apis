package com.feros.api.service.impl;

import com.feros.api.dto.request.LoginRequest;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.entity.Role;
import com.feros.api.entity.User;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.UserRepository;
import com.feros.api.service.AuthService;
import com.feros.api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {

        // 1. Find user by phone
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new FerosException(
                        "Invalid mobile number or PIN",
                        HttpStatus.UNAUTHORIZED
                ));

        // 2. Check if user is active
        if (!user.getIsActive()) {
            throw new FerosException(
                    "Your account is inactive. Please contact admin.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // 3. Validate PIN
        if (!passwordEncoder.matches(request.getPin(), user.getPin())) {
            throw new FerosException(
                    "Invalid mobile number or PIN",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // 4. Get primary role
        String role = user.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .findFirst()
                .orElseThrow(() -> new FerosException(
                        "No role assigned to user",
                        HttpStatus.UNAUTHORIZED
                ));

        // 5. Get tenant info
        Long tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        String companyName = user.getTenant() != null ? user.getTenant().getCompanyName() : "FEROS";
        String logoUrl = user.getTenant() != null ? user.getTenant().getLogoUrl() : null;

        // 6. Generate JWT token
        String token = jwtUtil.generateToken(
                user.getId(),
                tenantId,
                user.getPhone(),
                role
        );

        // 7. Return response
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .role(role)
                .tenantId(tenantId)
                .companyName(companyName)
                .logoUrl(logoUrl)
                .isPinResetRequired(user.getIsPinResetRequired())
                .build();
    }
}