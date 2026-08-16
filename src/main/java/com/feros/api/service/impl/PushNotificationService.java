package com.feros.api.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    @Async
    public void sendToTokens(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) return;
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .addAllTokens(tokens)
                    .build();
            var result = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Push sent: {} success, {} failure", result.getSuccessCount(), result.getFailureCount());
        } catch (Exception e) {
            log.warn("Push notification failed: {}", e.getMessage());
        }
    }
}
