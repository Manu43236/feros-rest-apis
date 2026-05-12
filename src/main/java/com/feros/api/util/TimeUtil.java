package com.feros.api.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * All date/time operations must use IST (Asia/Kolkata, UTC+5:30).
 * Never call LocalDate.now() or LocalDateTime.now() directly —
 * those use the JVM default timezone which is UTC on EC2.
 */
public class TimeUtil {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public static LocalDate today() {
        return LocalDate.now(IST);
    }

    public static LocalDateTime nowIst() {
        return LocalDateTime.now(IST);
    }

    private TimeUtil() {}
}
