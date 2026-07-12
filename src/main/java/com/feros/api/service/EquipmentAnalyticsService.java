package com.feros.api.service;

import com.feros.api.dto.response.EquipmentAnalyticsResponse;

import java.time.LocalDate;

public interface EquipmentAnalyticsService {
    EquipmentAnalyticsResponse getAnalytics(LocalDate from, LocalDate to);
}
