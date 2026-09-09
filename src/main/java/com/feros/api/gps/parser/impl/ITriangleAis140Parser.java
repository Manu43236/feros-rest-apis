package com.feros.api.gps.parser.impl;

import com.feros.api.entity.GpsDevice;
import com.feros.api.gps.model.GpsPing;
import com.feros.api.gps.parser.GpsPacketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Parser for iTriangle BHARAT101 2G — AIS-140 protocol.
 * Full field mapping implemented after verifying real packet output.
 */
@Component
@Slf4j
public class ITriangleAis140Parser implements GpsPacketParser {

    @Override
    public String parserKey() {
        return "ITRIANGLE_AIS140_V2";
    }

    @Override
    public Optional<GpsPing> parse(GpsDevice device, String rawFrame) {
        // TODO: implement field-by-field parsing once real packet format is confirmed
        // Protocol: 56-field comma-delimited ASCII, ends with *CHECKSUM\r\n
        // Time field is UTC — always store as UTC
        log.debug("ITriangleAis140Parser.parse() not yet implemented — awaiting real packet data");
        return Optional.empty();
    }
}
