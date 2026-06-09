package com.feros.api.service;

import com.feros.api.dto.response.TechnicianSummaryResponse;
import com.feros.api.dto.response.ServiceManagerDashboardResponse;

import java.util.List;

public interface ServiceManagerService {
    ServiceManagerDashboardResponse getDashboard();
    List<TechnicianSummaryResponse> getTechnicians();
}
