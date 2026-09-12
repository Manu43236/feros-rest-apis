package com.feros.api.gps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GpsPingCleanupJob {

    private final JdbcTemplate jdbc;

    // Every night at 11:59:59 PM IST — keep only the latest ping per vehicle
    @Scheduled(cron = "59 59 23 * * *", zone = "Asia/Kolkata")
    public void cleanup() {
        int deleted = jdbc.update("""
            DELETE FROM gps_pings
            WHERE id NOT IN (
                SELECT max_id FROM (
                    SELECT MAX(id) AS max_id FROM gps_pings GROUP BY vehicle_id
                ) AS latest
            )
            """);
        log.info("GPS ping cleanup: deleted {} rows, kept 1 per vehicle", deleted);
    }
}
