package com.feros.api.gps.handler;

import com.feros.api.enums.GpsConnectionType;

public interface GpsConnectionHandler {

    GpsConnectionType connectionType();

    void start();

    void stop();
}
