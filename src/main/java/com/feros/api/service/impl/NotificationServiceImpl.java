package com.feros.api.service.impl;

import com.feros.api.dto.request.BroadcastNotificationRequest;
import com.feros.api.dto.response.NotificationResponse;
import com.feros.api.entity.Notification;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.User;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.NotificationRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.UserRepository;
import com.feros.api.repository.UserSessionRepository;
import com.feros.api.service.NotificationService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    public List<NotificationResponse> getMyNotifications() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();
        return notificationRepository.findForUser(tenantId, userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();
        return notificationRepository.countUnread(tenantId, userId);
    }

    @Override
    public void markAllRead() {
        Long tenantId = SecurityUtil.getCurrentTenantId();
        Long userId = SecurityUtil.getCurrentUserId();
        notificationRepository.markAllRead(tenantId, userId);
    }

    @Override
    public void broadcast(BroadcastNotificationRequest request) {
        Tenant tenant = null;
        if (request.getTenantId() != null) {
            tenant = tenantRepository.findById(request.getTenantId()).orElse(null);
        }
        Notification notification = Notification.builder()
                .tenant(tenant)
                .type(NotificationType.BROADCAST)
                .title(request.getTitle())
                .message(request.getMessage())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void sendToTenant(Tenant tenant, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .tenant(tenant)
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void sendToUser(Tenant tenant, User user, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .tenant(tenant)
                .userId(user.getId())
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void sendToRoles(Tenant tenant, List<RoleName> roles, NotificationType type, String title, String message) {
        List<User> users = userRepository.findByTenantIdAndRoleNames(tenant.getId(), roles);
        List<Long> userIds = users.stream().map(User::getId).toList();
        for (User user : users) {
            Notification notification = Notification.builder()
                    .tenant(tenant)
                    .userId(user.getId())
                    .type(type)
                    .title(title)
                    .message(message)
                    .build();
            notificationRepository.save(notification);
        }
        List<String> tokens = userSessionRepository.findFcmTokensByUserIds(userIds);
        pushNotificationService.sendToTokens(tokens, title, message);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
