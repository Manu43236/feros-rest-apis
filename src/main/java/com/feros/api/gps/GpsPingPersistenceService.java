package com.feros.api.gps;

import com.feros.api.entity.GpsDevice;
import com.feros.api.gps.model.GpsPing;
import com.feros.api.repository.GpsDeviceRepository;
import com.feros.api.repository.GpsPingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsPingPersistenceService {

    private final GpsPingRepository pingRepo;
    private final GpsDeviceRepository deviceRepo;

    public void save(GpsPing ping) {
        // Dedup: device_id + recorded_at_utc is unique — duplicate on reconnect/history replay
        if (pingRepo.existsByDeviceIdAndRecordedAtUtc(ping.getDeviceId(), ping.getRecordedAtUtc())) {
            log.debug("GPS duplicate ping skipped — device {} at {}", ping.getDeviceId(), ping.getRecordedAtUtc());
            return;
        }
        try {
            pingRepo.save(ping);
            updateLastPingAt(ping.getDeviceId(), ping.getRecordedAtUtc());
        } catch (DataIntegrityViolationException e) {
            // Race condition on concurrent reconnect — safe to ignore
            log.debug("GPS duplicate ping (race) — device {} at {}", ping.getDeviceId(), ping.getRecordedAtUtc());
        }
    }

    private void updateLastPingAt(Long deviceId, LocalDateTime recordedAtUtc) {
        deviceRepo.findById(deviceId).ifPresent(device -> {
            // Only update if this ping is newer than the current lastPingAt
            if (device.getLastPingAt() == null || recordedAtUtc.isAfter(device.getLastPingAt())) {
                device.setLastPingAt(recordedAtUtc);
                deviceRepo.save(device);
            }
        });
    }
}
