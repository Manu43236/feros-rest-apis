# FEROS Module Plan: Vehicle Reports

## Summary
A read-only Vehicle Reports sub-module under the Reports major module. Gives ADMIN and OFFICE_STAFF users 6 configurable reports about their fleet — fleet status, utilization, fuel & mileage, breakdowns, document expiry, and maintenance. Every report supports date range filters, on-demand generation, and download as CSV or PDF.

## Requirements
- **Who**: ADMIN, OFFICE_STAFF (web only — no mobile)
- **Trigger**: User opens Reports → Vehicle Reports, sets date range, clicks Generate
- **Data captured**: None — read-only queries on existing tables
- **Approval flow**: None
- **Linked to**: vehicles, vehicle_fuel_logs, vehicle_meter_readings, vehicle_breakdowns, vehicle_documents, vehicle_services, vehicle_service_tasks, order_vehicle_allocations, lrs
- **Mobile**: No
- **Calculations**:
  - Utilization % = days on trip ÷ total days in period × 100
  - Total KM = closing odometer − opening odometer
  - Mileage = total KM ÷ total litres filled
  - Days Lost = breakdown resolved_at − breakdown_date
  - Document status = GREEN (valid) / AMBER (≤30 days) / RED (expired)
- **Export**: CSV and PDF — both required on every report

## Mirror Module
Dashboard — same pattern: read-only GET endpoints, service interface + impl, @PreAuthorize, no CRUD.

---

## Layer 1: Database
**No new tables required.** All queries read from existing tables.

**Tables used:**
- `vehicles` + `vehicle_statuses`
- `vehicle_fuel_logs`
- `vehicle_meter_readings`
- `vehicle_breakdowns`
- `vehicle_documents` + `document_types`
- `vehicle_services` + `vehicle_service_tasks` + `service_task_types`
- `order_vehicle_allocations` + `lrs`

**No pom.xml changes needed:**
- OpenCSV 5.9 — already present
- OpenPDF 2.0.3 (com.github.librepdf) — already present

**Validation**: `mvn compile` succeeds after pom.xml change.

---

## Layer 2: API (Backend)

**Files to create:**
- `src/main/java/com/feros/api/controller/ReportController.java` — CREATE
- `src/main/java/com/feros/api/service/ReportService.java` — CREATE (interface)
- `src/main/java/com/feros/api/service/impl/ReportServiceImpl.java` — CREATE
- `src/main/java/com/feros/api/dto/response/report/FleetStatusReportRow.java` — CREATE
- `src/main/java/com/feros/api/dto/response/report/VehicleUtilizationRow.java` — CREATE
- `src/main/java/com/feros/api/dto/response/report/FuelMileageRow.java` — CREATE
- `src/main/java/com/feros/api/dto/response/report/BreakdownReportRow.java` — CREATE
- `src/main/java/com/feros/api/dto/response/report/DocumentExpiryRow.java` — CREATE
- `src/main/java/com/feros/api/dto/response/report/MaintenanceServiceRow.java` — CREATE
- `src/main/java/com/feros/api/util/ReportExportUtil.java` — CREATE (CSV + PDF helpers)

**Endpoints:**

| Method | Route | Description | Params |
|--------|-------|-------------|--------|
| GET | `/api/v1/reports/vehicles/fleet-status` | Fleet status snapshot | `date` (optional, default today) |
| GET | `/api/v1/reports/vehicles/fleet-status/export` | Download fleet status | `date`, `format=csv\|pdf` |
| GET | `/api/v1/reports/vehicles/utilization` | Utilization per vehicle | `startDate`, `endDate` |
| GET | `/api/v1/reports/vehicles/utilization/export` | Download utilization | `startDate`, `endDate`, `format=csv\|pdf` |
| GET | `/api/v1/reports/vehicles/fuel-mileage` | Fuel & mileage per vehicle | `startDate`, `endDate` |
| GET | `/api/v1/reports/vehicles/fuel-mileage/export` | Download fuel & mileage | `startDate`, `endDate`, `format=csv\|pdf` |
| GET | `/api/v1/reports/vehicles/breakdowns` | All breakdowns in period | `startDate`, `endDate` |
| GET | `/api/v1/reports/vehicles/breakdowns/export` | Download breakdowns | `startDate`, `endDate`, `format=csv\|pdf` |
| GET | `/api/v1/reports/vehicles/document-expiry` | Document expiry status | `days` (default 30) |
| GET | `/api/v1/reports/vehicles/document-expiry/export` | Download doc expiry | `days`, `format=csv\|pdf` |
| GET | `/api/v1/reports/vehicles/maintenance-service` | Services in period | `startDate`, `endDate` |
| GET | `/api/v1/reports/vehicles/maintenance-service/export` | Download maintenance | `startDate`, `endDate`, `format=csv\|pdf` |

**Auth:** `@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OFFICE_STAFF')")`

**Export response headers:**
- CSV: `Content-Type: text/csv`, `Content-Disposition: attachment; filename="report.csv"`
- PDF: `Content-Type: application/pdf`, `Content-Disposition: attachment; filename="report.pdf"`

**Validation:** Server starts. All 12 endpoints respond with correct data.

---

## Layer 3: Web (Frontend)

**Files to create:**
- `src/pages/reports/ReportsPage.tsx` — main reports shell with sub-module tabs
- `src/pages/reports/VehicleReportsPage.tsx` — vehicle reports tab content (6 report cards)
- `src/api/reports.ts` — all report API calls

**Files to modify:**
- `src/App.tsx` — add `/reports` route
- `src/layouts/AppLayout.tsx` — add Reports to ADMIN_NAV and OFFICE_STAFF_NAV
- `src/types/index.ts` — add all 6 report row types

**Web UI Design:**
```
/reports
└── ReportsPage (tab shell)
    └── "Vehicle Reports" tab (active for now)
        └── VehicleReportsPage
            ├── Report selector (6 cards in a grid)
            └── Selected report panel:
                ├── Filter bar (date range + Generate button)
                ├── Download buttons (CSV | PDF)
                ├── Results table (columns per report)
                └── Summary row (totals)
```

**Sidebar addition (ADMIN + OFFICE_STAFF):**
```tsx
// New section in ADMIN_NAV and OFFICE_STAFF_NAV
{
  section: 'Reports',
  icon: BarChart2,  // from lucide-react
  items: [
    { to: '/reports', label: 'Reports', icon: BarChart2 },
  ],
}
```

**Validation:** `npm run build` — zero TypeScript errors.

---

## Layer 4: Mobile
**Skipped** — Reports is web-only.

---

## Step-by-Step Tasks

### Layer 1 Tasks
- Task 1.1: Add iText7 dependency to `pom.xml`
- Task 1.2: Run `mvn compile` — confirm clean

### Layer 2 Tasks
- Task 2.1: Create 6 report response DTOs in `dto/response/report/`
- Task 2.2: Create `ReportService` interface with 12 method signatures (6 data + 6 export)
- Task 2.3: Create `ReportExportUtil` with `toCsv()` and `toPdf()` helpers
- Task 2.4: Implement `ReportServiceImpl` — fleet status query
- Task 2.5: Implement `ReportServiceImpl` — vehicle utilization query
- Task 2.6: Implement `ReportServiceImpl` — fuel & mileage query
- Task 2.7: Implement `ReportServiceImpl` — breakdown report query
- Task 2.8: Implement `ReportServiceImpl` — document expiry query
- Task 2.9: Implement `ReportServiceImpl` — maintenance & service query
- Task 2.10: Create `ReportController` — all 12 endpoints wired to service
- Task 2.11: Start server, test all 6 data endpoints respond correctly

### Layer 3 Tasks
- Task 3.1: Add 6 report row types to `src/types/index.ts`
- Task 3.2: Create `src/api/reports.ts` — all 12 API calls (6 data + 6 export)
- Task 3.3: Build `ReportsPage.tsx` — tab shell (Vehicle Reports tab active, others disabled for now)
- Task 3.4: Build `VehicleReportsPage.tsx` — 6 report cards + selected report panel with filters, table, download
- Task 3.5: Add `/reports` route to `App.tsx`
- Task 3.6: Add Reports section to `ADMIN_NAV` and `OFFICE_STAFF_NAV` in `AppLayout.tsx`
- Task 3.7: Run `npm run build` — zero errors

---

## Acceptance Criteria
- [ ] `mvn compile` clean after pom.xml change
- [ ] All 12 API endpoints respond with correct tenant-scoped data
- [ ] CSV export downloads valid CSV for each of the 6 reports
- [ ] PDF export downloads valid PDF for each of the 6 reports
- [ ] Web page loads at `/reports`
- [ ] Reports sidebar item visible for ADMIN and OFFICE_STAFF
- [ ] All 6 report cards visible, filters work, table renders correctly
- [ ] Date range presets work: Today, This Week, This Month, Custom
- [ ] `npm run build` zero TypeScript errors
- [ ] No data from other tenants appears in any report (tenant isolation confirmed)
