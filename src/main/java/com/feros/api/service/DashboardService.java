package com.feros.api.service;

import com.feros.api.dto.response.DashboardResponse;
import com.feros.api.dto.response.ExpiryAlertResponse;

public interface DashboardService {
    DashboardResponse getDashboard();
    ExpiryAlertResponse getExpiryAlerts(int days);
}
