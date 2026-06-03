package com.feros.api.service;

import com.feros.api.dto.response.report.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<FleetStatusRow> getFleetStatus(LocalDate date);

    List<VehicleUtilizationRow> getVehicleUtilization(LocalDate startDate, LocalDate endDate);

    List<FuelMileageRow> getFuelMileage(LocalDate startDate, LocalDate endDate);

    List<BreakdownReportRow> getBreakdowns(LocalDate startDate, LocalDate endDate);

    List<DocumentExpiryRow> getDocumentExpiry(int days);

    List<MaintenanceServiceRow> getMaintenanceService(LocalDate startDate, LocalDate endDate);
}
