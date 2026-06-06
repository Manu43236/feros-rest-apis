package com.feros.api.enums;

public enum GpsVehicleStatus {
    MOVING,   // speed > 5 km/h
    IDLE,     // ignition ON, speed <= 5 km/h
    STOPPED,  // ignition OFF
    OFFLINE   // no update in last 30 minutes
}
