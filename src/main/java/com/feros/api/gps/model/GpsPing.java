package com.feros.api.gps.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Normalized GPS ping — device-agnostic output from any GpsPacketParser.
 * Fields confirmed against real packet data before persisting to DB.
 */
@Data
@Builder
public class GpsPing {

    private Long deviceId;
    private Long vehicleId;
    private Long tenantId;

    private Instant recordedAtUtc;   // from device packet (always UTC)
    private Instant receivedAt;       // server wall clock when packet arrived

    private BigDecimal latitude;      // signed: negative if S
    private BigDecimal longitude;     // signed: negative if W
    private BigDecimal speedKmh;
    private Integer   heading;        // 0–360 degrees
    private BigDecimal altitude;      // metres above sea level

    private Boolean ignitionOn;
    private Boolean gpsFixValid;

    private String  packetType;       // NR, IN, IF, EA, HP, BD, etc.
    private Integer alertId;          // message ID from device
    private Boolean isHistory;        // true = H (stored), false = L (live)
    private Integer frameNumber;      // for deduplication

    private BigDecimal batteryVoltage; // internal device battery (V)
    private BigDecimal mainVoltage;    // vehicle main power (V)
    private Integer    gsmSignal;      // 0–31

    private String rawFrame;           // original packet string (debug only)
}
