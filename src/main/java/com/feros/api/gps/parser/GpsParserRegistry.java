package com.feros.api.gps.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GpsParserRegistry {

    private final Map<String, GpsPacketParser> parsers;

    public GpsParserRegistry(List<GpsPacketParser> parserList) {
        this.parsers = parserList.stream()
                .collect(Collectors.toMap(GpsPacketParser::parserKey, p -> p));
        log.info("GPS parsers registered: {}", parsers.keySet());
    }

    public Optional<GpsPacketParser> getParser(String parserKey) {
        return Optional.ofNullable(parsers.get(parserKey));
    }
}
