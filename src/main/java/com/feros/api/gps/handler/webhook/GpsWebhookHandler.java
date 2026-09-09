package com.feros.api.gps.handler.webhook;

import com.feros.api.enums.GpsConnectionType;
import com.feros.api.gps.handler.GpsConnectionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Future: webhook-based GPS integrations where the device provider pushes to us.
 * The actual receiver will be a REST endpoint: POST /api/v1/gps/webhook/{parserKey}
 * This class exists for consistency with the handler registry pattern.
 */
@Component
@Slf4j
public class GpsWebhookHandler implements GpsConnectionHandler {

    @Override
    public GpsConnectionType connectionType() {
        return GpsConnectionType.WEBHOOK;
    }

    @Override
    public void start() {
        // Webhook is push-based — no server to start here.
        // Incoming payloads are received by a dedicated REST controller endpoint.
        log.info("GPS Webhook handler registered (served via POST /api/v1/gps/webhook/{parserKey})");
    }

    @Override
    public void stop() {
        // no-op
    }
}
