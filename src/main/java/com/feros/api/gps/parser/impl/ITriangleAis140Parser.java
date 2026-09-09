package com.feros.api.gps.parser.impl;

import com.feros.api.entity.GpsDevice;
import com.feros.api.gps.model.GpsPing;
import com.feros.api.gps.parser.GpsPacketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Parser for iTriangle BHARAT101 2G — AIS-140 protocol.
 *
 * Real packet format (verified from live device IMEI 358250331741362):
 *
 * Login:   $LGN,{vehicleReg},{imei},{firmware},{protocol},{lat},{lng}*
 * Tracking: $PVT,{vendor},{firmware},{packetType},{alertId},{L|H},{imei},
 *            {vehicleReg},{gpsFix},{DDMMYYYY},{HHMMSS(UTC)},{lat},{N|S},
 *            {lng},{E|W},{speed},{heading},{satellites},{altitude},{pdop},
 *            {hdop},{operator},{ignition},{mainPower},{mainVoltage},
 *            {batteryVoltage},{emergency},{tamper},{gsmSignal},{mcc},{mnc},
 *            {lacServing},{cidServing},{lac1},{cid1},{sig1},{lac2},{cid2},{sig2},
 *            {lac3},{cid3},{sig3},{lac4},{cid4},{sig4},{din},{dout},{frameNum},{checksum}*
 *
 * Field indices (0-based after strip and split):
 *   3=packetType, 4=alertId, 5=L/H, 6=IMEI, 8=gpsFix,
 *   9=date(DDMMYYYY), 10=time(HHMMSS UTC),
 *   11=lat, 12=latDir, 13=lng, 14=lngDir,
 *   15=speed, 16=heading, 18=altitude,
 *   22=ignition, 24=mainVoltage, 25=batteryVoltage, 28=gsmSignal,
 *   47=frameNumber
 *
 * Time field is UTC — store as UTC, display as IST (+5:30) in UI.
 */
@Component
@Slf4j
public class ITriangleAis140Parser implements GpsPacketParser {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

    @Override
    public String parserKey() {
        return "ITRIANGLE_AIS140_V2";
    }

    @Override
    public Optional<GpsPing> parse(GpsDevice device, String rawFrame) {
        try {
            String frame = rawFrame.strip();

            // Only parse tracking packets
            if (!frame.startsWith("$PVT")) return Optional.empty();

            // Strip checksum: format is DATA,CHECKSUM* — drop from last *
            int starIdx = frame.lastIndexOf('*');
            if (starIdx > 0) frame = frame.substring(0, starIdx);

            String[] f = frame.split(",");

            // Need at least up to frameNumber (index 47) + checksum was stripped
            if (f.length < 48) {
                log.warn("AIS140: short packet ({} fields) from IMEI {}", f.length, device.getDeviceIdentifier());
                return Optional.empty();
            }

            // Skip invalid GPS fix
            if (!"1".equals(f[8])) return Optional.empty();

            // Parse UTC timestamp — field 9 = DDMMYYYY, field 10 = HHMMSS
            LocalDateTime recordedAtUtc = parseUtcTimestamp(f[9], f[10]);
            if (recordedAtUtc == null) return Optional.empty();

            // Lat/Lng with direction
            BigDecimal lat = new BigDecimal(f[11]);
            if ("S".equalsIgnoreCase(f[12])) lat = lat.negate();

            BigDecimal lng = new BigDecimal(f[13]);
            if ("W".equalsIgnoreCase(f[14])) lng = lng.negate();

            return Optional.of(GpsPing.builder()
                    .deviceId(device.getId())
                    .vehicleId(device.getVehicle().getId())
                    .tenantId(device.getTenant().getId())
                    .recordedAtUtc(recordedAtUtc)
                    .receivedAt(LocalDateTime.now())
                    .latitude(lat)
                    .longitude(lng)
                    .speedKmh(new BigDecimal(f[15]))
                    .heading(Integer.parseInt(f[16]))
                    .altitude(new BigDecimal(f[18]))
                    .ignitionOn("1".equals(f[22]))
                    .gpsFixValid(true)
                    .packetType(f[3])
                    .alertId(Integer.parseInt(f[4]))
                    .isHistory("H".equalsIgnoreCase(f[5]))
                    .frameNumber(Integer.parseInt(f[47]))
                    .mainVoltage(new BigDecimal(f[24]))
                    .batteryVoltage(new BigDecimal(f[25]))
                    .gsmSignal(Integer.parseInt(f[28]))
                    .build());

        } catch (Exception e) {
            log.warn("AIS140: parse error from IMEI {}: {}", device.getDeviceIdentifier(), e.getMessage());
            return Optional.empty();
        }
    }

    private LocalDateTime parseUtcTimestamp(String dateStr, String timeStr) {
        try {
            return LocalDateTime.parse(dateStr + timeStr, DATE_TIME_FMT);
        } catch (Exception e) {
            log.warn("AIS140: bad timestamp date={} time={}", dateStr, timeStr);
            return null;
        }
    }
}
