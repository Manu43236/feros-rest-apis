package com.feros.api.service;

import com.feros.api.dto.response.DashboardResponse;
import com.feros.api.dto.response.DriverDashboardResponse;
import com.feros.api.dto.response.ExpiryAlertResponse;
import com.feros.api.dto.response.SupervisorDashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboard();
    ExpiryAlertResponse getExpiryAlerts(int days);
    DriverDashboardResponse getDriverDashboard();
    SupervisorDashboardResponse getSupervisorDashboard();
}
