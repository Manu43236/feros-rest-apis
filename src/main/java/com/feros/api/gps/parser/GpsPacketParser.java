package com.feros.api.gps.parser;

import com.feros.api.entity.GpsDevice;
import com.feros.api.gps.model.GpsPing;

import java.util.Optional;

public interface GpsPacketParser {

    /** Must match parser_key stored in gps_device_models table. */
    String parserKey();

    /**
     * Parse one raw frame into a normalized GpsPing.
     * Returns empty if the frame should be skipped (health packet, bad checksum, etc.).
     */
    Optional<GpsPing> parse(GpsDevice device, String rawFrame);
}
