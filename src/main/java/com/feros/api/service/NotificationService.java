package com.feros.api.service;

import com.feros.api.dto.request.BroadcastNotificationRequest;
import com.feros.api.dto.response.NotificationResponse;
import com.feros.api.entity.Tenant;
import com.feros.api.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications();
    long getUnreadCount();
    void markAllRead();
    void broadcast(BroadcastNotificationRequest request);
    void sendToTenant(Tenant tenant, NotificationType type, String title, String message);
    void sendToRoles(Tenant tenant, List<com.feros.api.enums.RoleName> roles, NotificationType type, String title, String message);
}
