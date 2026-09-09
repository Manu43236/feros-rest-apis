package com.feros.api.gps.handler.rest;

import com.feros.api.enums.GpsConnectionType;
import com.feros.api.gps.handler.GpsConnectionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Future: scheduled polling of REST API endpoints per device.
 * Used for integrations like BlackBuck, TATA Motors, etc. that push via REST.
 */
@Component
@Slf4j
public class GpsRestApiHandler implements GpsConnectionHandler {

    @Override
    public GpsConnectionType connectionType() {
        return GpsConnectionType.REST_API;
    }

    @Override
    public void start() {
        // TODO: Phase 3+ — per-device scheduled polling using RestTemplate
        // Each REST_API device in gps_devices will have credentials JSON with baseUrl + accessToken
        log.info("GPS REST API handler registered (not yet active — future phase)");
    }

    @Override
    public void stop() {
        // no-op
    }
}
