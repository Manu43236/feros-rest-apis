package com.feros.api.service.impl;

import com.feros.api.dto.request.ChangePinRequest;
import com.feros.api.dto.request.LoginRequest;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.entity.UserSession;
import com.feros.api.repository.UserSessionRepository;
import com.feros.api.util.SecurityUtil;
import com.feros.api.entity.User;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.UserRepository;
import com.feros.api.service.AuthService;
import com.feros.api.service.S3Service;
import com.feros.api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress) {

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
        String logoKey = user.getTenant() != null ? user.getTenant().getLogoUrl() : null;
        String logoUrl = logoKey != null ? s3Service.getPublicUrl(logoKey) : null;

        // 6. Generate JWT token
        String token = jwtUtil.generateToken(
                user.getId(),
                tenantId,
                user.getPhone(),
                role
        );

        // 7. Upsert session — replaces any existing session for this user + device type
        LocalDateTime now = LocalDateTime.now();
        UserSession session = userSessionRepository
                .findByUserIdAndDeviceType(user.getId(), request.getDeviceType())
                .orElse(UserSession.builder()
                        .user(user)
                        .deviceType(request.getDeviceType())
                        .loggedInAt(now)
                        .build());

        session.setToken(token);
        session.setIpAddress(ipAddress);
        session.setDeviceInfo(request.getDeviceInfo());
        session.setFcmToken(request.getFcmToken());
        session.setAppVersion(request.getAppVersion());
        session.setLoggedInAt(now);
        session.setLoggedOutAt(null);
        session.setLastActiveAt(now);
        userSessionRepository.save(session);

        // 8. Return response
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

    @Override
    public void logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        String token = SecurityUtil.getCurrentToken();

        userSessionRepository.findByToken(token).ifPresent(session -> {
            session.setLoggedOutAt(LocalDateTime.now());
            userSessionRepository.save(session);
            userSessionRepository.deleteByUserIdAndDeviceType(userId, session.getDeviceType());
        });
    }

    @Override
    public void changePin(ChangePinRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPin(), user.getPin())) {
            throw new FerosException("Current PIN is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPin(passwordEncoder.encode(request.getNewPin()));
        user.setIsPinResetRequired(false);
        userRepository.save(user);
    }
}