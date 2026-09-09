package com.feros.api.gps.handler.tcp;

import com.feros.api.entity.GpsDevice;
import com.feros.api.gps.parser.GpsPacketParser;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TcpSessionContext {

    private GpsDevice device;
    private GpsPacketParser parser;
    private String remoteAddress;
    private Instant connectedAt;
}
