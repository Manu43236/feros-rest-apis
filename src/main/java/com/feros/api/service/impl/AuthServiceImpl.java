package com.feros.api.service.impl;

import com.feros.api.dto.request.ChangePinRequest;
import com.feros.api.dto.request.LoginRequest;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.entity.UserSession;
import com.feros.api.repository.UserSessionRepository;
import com.feros.api.util.SecurityUtil;
import com.feros.api.entity.User;
import com.feros.api.exception.AccountLockedException;
import com.feros.api.exception.FerosException;
import com.feros.api.entity.RbacLoginAccess;
import com.feros.api.enums.DeviceType;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.RbacLoginAccessRepository;
import com.feros.api.repository.StaffProfileRepository;
import com.feros.api.repository.UserRepository;
import com.feros.api.service.AuthService;
import com.feros.api.service.NotificationService;
import com.feros.api.service.RoleModuleAccessService;
import com.feros.api.service.S3Service;
import com.feros.api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 5;

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;
    private final RbacLoginAccessRepository rbacLoginAccessRepository;
    private final RoleModuleAccessService roleModuleAccessService;
    private final NotificationService notificationService;
    private final StaffProfileRepository staffProfileRepository;

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

        // 3. Check lockout — if locked and timer not expired, reject
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AccountLockedException(user.getLockedUntil());
            }
            // Lockout expired — clear it automatically
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        // 4. Validate PIN
        if (!passwordEncoder.matches(request.getPin(), user.getPin())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                // Lock the account
                LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(lockedUntil);
                userRepository.save(user);
                // Notify tenant admins
                if (user.getTenant() != null) {
                    notificationService.sendToRoles(
                            user.getTenant(),
                            List.of(RoleName.ADMIN),
                            NotificationType.PIN_RESET_REQUEST,
                            "User Locked Out",
                            user.getName() + " (" + user.getPhone() + ") has been locked out after " + MAX_FAILED_ATTEMPTS + " failed login attempts. Reset their PIN to unlock immediately."
                    );
                }
                throw new AccountLockedException(lockedUntil);
            }
            user.setFailedLoginAttempts(attempts);
            userRepository.save(user);
            throw new com.feros.api.exception.WrongPinException(attempts);
        }

        // 5. PIN correct — clear failed attempts
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        // 6. Get primary role
        String role = user.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .findFirst()
                .orElseThrow(() -> new FerosException(
                        "No role assigned to user",
                        HttpStatus.UNAUTHORIZED
                ));

        // 7. Get tenant info
        Long tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        String companyName = user.getTenant() != null ? user.getTenant().getCompanyName() : "FEROS";
        String logoKey = user.getTenant() != null ? user.getTenant().getLogoUrl() : null;
        String logoUrl = logoKey != null ? s3Service.getPublicUrl(logoKey) : null;

        // 7b. Enforce login access RBAC (skip for SUPER_ADMIN and ADMIN)
        if (tenantId != null && !role.equals("SUPER_ADMIN") && !role.equals("ADMIN")) {
            try {
                RoleName roleName = RoleName.valueOf(role);
                rbacLoginAccessRepository
                        .findByTenantIdAndRoleAndPlatform(tenantId, roleName, request.getDeviceType())
                        .ifPresent(entry -> {
                            if (Boolean.FALSE.equals(entry.getAllowed())) {
                                String platform = request.getDeviceType().name().toLowerCase();
                                throw new com.feros.api.exception.FerosException(
                                        "Your role is not permitted to log in on the " + platform + " platform.",
                                        HttpStatus.FORBIDDEN);
                            }
                        });
            } catch (IllegalArgumentException ignored) {
                // Unknown role — allow through
            }
        }

        // 8. Generate JWT token
        String token = jwtUtil.generateToken(
                user.getId(),
                tenantId,
                user.getPhone(),
                role
        );

        // 9. Create new session — displace any existing session for this user + device type first
        LocalDateTime now = LocalDateTime.now();
        userSessionRepository.deleteAllByUserIdAndDeviceType(user.getId(), request.getDeviceType());
        UserSession session = UserSession.builder()
                .user(user)
                .deviceType(request.getDeviceType())
                .token(token)
                .ipAddress(ipAddress)
                .deviceInfo(request.getDeviceInfo())
                .fcmToken(request.getFcmToken())
                .appVersion(request.getAppVersion())
                .loggedInAt(now)
                .lastActiveAt(now)
                .build();
        userSessionRepository.save(session);

        // 10. Resolve allowed modules (null for ADMIN/SUPER_ADMIN — means all visible)
        List<String> allowedModules = null;
        if (tenantId != null && !role.equals("SUPER_ADMIN") && !role.equals("ADMIN")) {
            try {
                RoleName roleName = RoleName.valueOf(role);
                allowedModules = roleModuleAccessService.getEnabledModules(tenantId, roleName);
            } catch (IllegalArgumentException ignored) {
                // Unknown role — no module filtering
            }
        }

        // 11. Resolve staff access flags (only for staff roles)
        Boolean canAccessVehicles = null;
        Boolean canAccessEquipment = null;
        if (tenantId != null) {
            var profileOpt = staffProfileRepository.findByUserIdAndIsActiveTrue(user.getId());
            if (profileOpt.isPresent()) {
                canAccessVehicles = profileOpt.get().getCanAccessVehicles();
                canAccessEquipment = profileOpt.get().getCanAccessEquipment();
            }
        }

        // 12. Return response
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
                .allowedModules(allowedModules)
                .moduleType(user.getTenant() != null ? user.getTenant().getModuleType() : null)
                .canAccessVehicles(canAccessVehicles)
                .canAccessEquipment(canAccessEquipment)
                .build();
    }

    @Override
    public void logout() {
        String token = SecurityUtil.getCurrentToken();
        userSessionRepository.findByToken(token).ifPresent(userSessionRepository::delete);
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
        user.setPlainPin(request.getNewPin());
        user.setIsPinResetRequired(false);
        userRepository.save(user);
    }

    @Override
    public void askPinReset(String phone) {
        // Find user by phone — silently ignore if not found (prevent phone enumeration)
        userRepository.findByPhone(phone).ifPresent(user -> {
            if (user.getTenant() != null) {
                notificationService.sendToRoles(
                        user.getTenant(),
                        List.of(RoleName.ADMIN),
                        NotificationType.PIN_RESET_REQUEST,
                        "PIN Reset Requested",
                        user.getName() + " (" + user.getPhone() + ") cannot log in and is requesting a PIN reset."
                );
            }
        });
    }
}
