package com.feros.api.service;

import com.feros.api.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** Flow-duration label formatting for the Maintenance & Service report. */
class MaintenanceDurationFormatTest {

    @Test
    void minutesOnly() {
        assertThat(ReportServiceImpl.formatDuration(Duration.ofMinutes(45), false)).isEqualTo("45m");
    }

    @Test
    void hoursAndMinutes() {
        assertThat(ReportServiceImpl.formatDuration(Duration.ofMinutes(390), false)).isEqualTo("6h 30m");
    }

    @Test
    void wholeHours() {
        assertThat(ReportServiceImpl.formatDuration(Duration.ofHours(2), false)).isEqualTo("2h");
    }

    @Test
    void daysAndHours() {
        assertThat(ReportServiceImpl.formatDuration(Duration.ofHours(52), false)).isEqualTo("2d 4h");
    }

    @Test
    void estimatePrefixedWithTilde() {
        assertThat(ReportServiceImpl.formatDuration(Duration.ofDays(2), true)).isEqualTo("~2d");
    }

    @Test
    void negativeClampsToZero() {
        assertThat(ReportServiceImpl.formatDuration(Duration.ofMinutes(-10), false)).isEqualTo("0m");
    }
}
