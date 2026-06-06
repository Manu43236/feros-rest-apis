# FEROS Module Plan: GPS Integration

## Summary
Integrate third-party GPS/telematics providers (starting with TATA Fleet Edge) into FEROS.
Tenants configure their GPS credentials once; all fitted vehicles appear on a live map.
Web only in V1. Admin configures, office staff monitors.

## Requirements
- Who: ADMIN (configure + view), OFFICE_STAFF (view)
- Trigger: Tenant sets up GPS provider credentials → vehicles show on map
- Surface: Web only (mobile V2)
- Provider: TATA Fleet Edge (V1 only, adapter pattern for future)
- Map: 80% map, 20% data strip (summary cards + vehicle list)
- Vehicles color-coded: Moving (green), Idle (yellow), Stopped (red), Offline (grey)
- Click dot → popup: vehicle number, driver, speed, last updated
- Web polls every 60s for fresh positions
- One tenant can have multiple GPS providers
- Fleet-level credentials (not per-vehicle)
- Vehicle auto-matched by registration number, manual fallback
- Credentials encrypted at rest (AES-256)

## Mirror Module
VehicleBreakdown — similar pattern: entity linked to vehicle + tenant, service + controller

---

## Layer 1: Database

**Files to create:**
- `src/main/resources/db/migration/V_gps_provider_configs.sql`
- `src/main/resources/db/migration/V_vehicle_gps_mappings.sql`

### V_gps_provider_configs.sql
```sql
CREATE TABLE gps_provider_configs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    provider_type   VARCHAR(50) NOT NULL,          -- TATA_FLEET_EDGE | BLACKBUCK | VAMOSYS
    display_name    VARCHAR(100),                  -- e.g. "TATA Fleet Edge - Main"
    client_id_enc   TEXT NOT NULL,                 -- AES-256 encrypted
    client_secret_enc TEXT NOT NULL,               -- AES-256 encrypted
    api_base_url    VARCHAR(255),                  -- overrideable per tenant if needed
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    last_sync_at    DATETIME,
    sync_status     VARCHAR(20) DEFAULT 'NEVER',   -- NEVER | OK | ERROR
    sync_error_msg  TEXT,
    created_at      DATETIME,
    updated_at      DATETIME,
    CONSTRAINT fk_gps_config_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
```

### V_vehicle_gps_mappings.sql
```sql
CREATE TABLE vehicle_gps_mappings (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    vehicle_id              BIGINT NOT NULL,
    gps_provider_config_id  BIGINT NOT NULL,
    provider_vehicle_id     VARCHAR(100) NOT NULL,  -- TATA's internal ID
    provider_reg_number     VARCHAR(50),            -- reg number as TATA knows it
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              DATETIME,
    updated_at              DATETIME,
    CONSTRAINT fk_gps_map_tenant   FOREIGN KEY (tenant_id)              REFERENCES tenants(id),
    CONSTRAINT fk_gps_map_vehicle  FOREIGN KEY (vehicle_id)             REFERENCES vehicles(id),
    CONSTRAINT fk_gps_map_config   FOREIGN KEY (gps_provider_config_id) REFERENCES gps_provider_configs(id),
    CONSTRAINT uq_vehicle_gps_mapping UNIQUE (vehicle_id, gps_provider_config_id)
);
```

**Validation:** Both migrations run clean. Tables created with correct FK constraints.

---

## Layer 2: API (Backend)

### Files to CREATE

**Enums:**
- `src/main/java/com/feros/api/enums/GpsProviderType.java`
- `src/main/java/com/feros/api/enums/GpsVehicleStatus.java`

**Entities:**
- `src/main/java/com/feros/api/entity/GpsProviderConfig.java`
- `src/main/java/com/feros/api/entity/VehicleGpsMapping.java`

**Repositories:**
- `src/main/java/com/feros/api/repository/GpsProviderConfigRepository.java`
- `src/main/java/com/feros/api/repository/VehicleGpsMappingRepository.java`

**DTOs — Request:**
- `src/main/java/com/feros/api/dto/request/GpsProviderConfigRequest.java`
- `src/main/java/com/feros/api/dto/request/VehicleGpsMappingRequest.java`

**DTOs — Response:**
- `src/main/java/com/feros/api/dto/response/GpsProviderConfigResponse.java`
- `src/main/java/com/feros/api/dto/response/VehicleGpsMappingResponse.java`
- `src/main/java/com/feros/api/dto/response/GpsFleetVehicleResponse.java`
- `src/main/java/com/feros/api/dto/response/GpsProviderVehicleResponse.java`

**Adapter (TATA Fleet Edge):**
- `src/main/java/com/feros/api/gps/GpsProviderAdapter.java`             (interface)
- `src/main/java/com/feros/api/gps/TataFleetEdgeAdapter.java`            (TATA impl)
- `src/main/java/com/feros/api/gps/dto/TataAuthResponse.java`            (TATA auth response)
- `src/main/java/com/feros/api/gps/dto/TataVehicleLocation.java`         (TATA location DTO)

**Utility:**
- `src/main/java/com/feros/api/util/EncryptionUtil.java`                 (AES-256 encrypt/decrypt)

**Service:**
- `src/main/java/com/feros/api/service/GpsService.java`                  (interface)
- `src/main/java/com/feros/api/service/impl/GpsServiceImpl.java`

**Controller:**
- `src/main/java/com/feros/api/controller/GpsController.java`

### Endpoints

| Method | Route | Roles | Purpose |
|--------|-------|-------|---------|
| GET    | `/api/v1/gps/configs`                    | ADMIN | List all GPS configs for tenant |
| POST   | `/api/v1/gps/configs`                    | ADMIN | Save new GPS provider config |
| PUT    | `/api/v1/gps/configs/:id`                | ADMIN | Update GPS config |
| DELETE | `/api/v1/gps/configs/:id`                | ADMIN | Remove GPS config |
| POST   | `/api/v1/gps/configs/:id/test`           | ADMIN | Test connection (auth + fetch 1 vehicle) |
| GET    | `/api/v1/gps/configs/:id/provider-vehicles` | ADMIN | Fetch vehicle list from provider (for mapping) |
| GET    | `/api/v1/gps/mappings`                   | ADMIN | List all vehicle-GPS mappings |
| POST   | `/api/v1/gps/mappings`                   | ADMIN | Create a vehicle-GPS mapping |
| DELETE | `/api/v1/gps/mappings/:id`               | ADMIN | Remove a mapping |
| GET    | `/api/v1/gps/fleet`                      | ADMIN, OFFICE_STAFF | Fleet map data — all IoT vehicles with latest location |

### GpsFleetVehicleResponse shape
```json
{
  "vehicleId": 1,
  "registrationNumber": "MH-12-AB-1234",
  "driverName": "Raju Kumar",
  "latitude": 18.5204,
  "longitude": 73.8567,
  "speedKmh": 45,
  "gpsStatus": "MOVING",
  "lastUpdatedAt": "2026-06-06T10:30:00",
  "providerType": "TATA_FLEET_EDGE"
}
```

### GpsVehicleStatus logic
- `MOVING`  — speed > 5 km/h
- `IDLE`    — ignition ON, speed <= 5 km/h
- `STOPPED` — ignition OFF
- `OFFLINE` — last update > 30 min ago (or provider returns no data)

### GpsProviderAdapter interface
```java
public interface GpsProviderAdapter {
    GpsProviderType getProviderType();
    List<GpsProviderVehicleResponse> fetchVehicles(GpsProviderConfig config);
    List<GpsFleetVehicleResponse> fetchLocations(GpsProviderConfig config, List<VehicleGpsMapping> mappings);
    boolean testConnection(GpsProviderConfig config);
}
```

### EncryptionUtil
- AES-256-GCM
- Key loaded from env var `GPS_ENCRYPTION_KEY` (32-byte base64)
- `encrypt(plaintext)` → base64 ciphertext
- `decrypt(ciphertext)` → plaintext
- Never log decrypted values

### Mirror: VehicleBreakdownController + VehicleBreakdownServiceImpl

**Validation:** Server starts. All 10 endpoints respond. `/gps/fleet` returns correct JSON.

---

## Layer 3: Web (Frontend)

### Files to CREATE
- `src/pages/gps/GpsTrackerPage.tsx`      — main map page (80/20 layout)
- `src/pages/gps/GpsSettingsPage.tsx`     — provider config + vehicle mapping
- `src/api/gps.ts`                         — API calls

### Files to MODIFY
- `src/types/index.ts`                     — add GPS types
- `src/App.tsx`                            — add /gps and /gps/settings routes
- `src/layouts/AppLayout.tsx`              — add "GPS Tracker" to Fleet section

### New npm dependency
```
react-leaflet + leaflet + @types/leaflet
```
OpenStreetMap tiles — free, no API key.

### GpsTrackerPage layout
```
┌────────────────────────────────────┐
│                                    │
│         MAP  (80% height)          │
│   vehicle dots, color-coded        │
│   click dot → popup                │
│                                    │
├────────────────────────────────────┤
│  [Total: 12] [Moving: 8] [Idle: 3] [Stopped: 1]  │  scrollable vehicle list  │
│              SUMMARY CARDS         │  reg no | driver | speed | status | time │
└────────────────────────────────────┘
                  20% height
```

- Map auto-fits bounds to show all vehicles on load
- Dot colors: green (MOVING), yellow (IDLE), red (STOPPED), grey (OFFLINE)
- Click dot → Leaflet popup: reg number, driver, speed, last updated
- Click row in vehicle list → map pans & opens popup for that vehicle
- Auto-refreshes every 60s via `refetchInterval`
- "Settings" button (top-right) → navigates to `/gps/settings`

### GpsSettingsPage layout
Two sections:
1. **GPS Providers** — list of configured providers, Add/Edit/Delete, Test Connection button
2. **Vehicle Mapping** — table of GPS provider vehicles vs FEROS vehicles, auto-matched rows highlighted, unmatched show "Link" button

### Nav item
Add to Fleet section in ADMIN_NAV and OFFICE_STAFF_NAV:
```ts
{ to: '/gps', label: 'GPS Tracker', icon: MapPin }
```
`MapPin` already available from lucide-react.

### Mirror: VehiclesPage.tsx + vehicles.ts api pattern

**Validation:** `npm run build` — zero TypeScript errors. Page loads. Map renders with tiles. API calls work.

---

## Step-by-Step Tasks

### Layer 1 Tasks
- 1.1: Create `V_gps_provider_configs.sql` migration
- 1.2: Create `V_vehicle_gps_mappings.sql` migration
- 1.3: Run migrations, verify tables created

### Layer 2 Tasks
- 2.1: Create `GpsProviderType` and `GpsVehicleStatus` enums
- 2.2: Create `GpsProviderConfig` and `VehicleGpsMapping` entities
- 2.3: Create repositories
- 2.4: Create `EncryptionUtil` (AES-256-GCM, key from env)
- 2.5: Create `GpsProviderAdapter` interface
- 2.6: Create `TataFleetEdgeAdapter` (auth + fetchVehicles + fetchLocations)
- 2.7: Create request/response DTOs
- 2.8: Create `GpsService` interface + `GpsServiceImpl`
- 2.9: Create `GpsController` with all 10 endpoints
- 2.10: Verify server starts, endpoints respond

### Layer 3 Tasks
- 3.1: Add GPS types to `src/types/index.ts`
- 3.2: Create `src/api/gps.ts`
- 3.3: Install `react-leaflet leaflet @types/leaflet`
- 3.4: Build `GpsTrackerPage.tsx` (80/20 layout, map, data strip)
- 3.5: Build `GpsSettingsPage.tsx` (provider config + vehicle mapping)
- 3.6: Add routes to `App.tsx`
- 3.7: Add "GPS Tracker" nav item to Fleet section in `AppLayout.tsx`
- 3.8: `npm run build` — zero errors

---

## Acceptance Criteria
- [ ] Both DB migrations run clean
- [ ] TATA Fleet Edge adapter authenticates and returns vehicle locations
- [ ] Credentials stored encrypted, never logged
- [ ] `/gps/fleet` returns correct GpsFleetVehicleResponse list
- [ ] GPS Tracker page renders map with color-coded vehicle dots
- [ ] 80% map / 20% data strip layout correct
- [ ] Summary cards show correct counts
- [ ] Vehicle list scrollable, click pans map
- [ ] Auto-refresh every 60s
- [ ] GPS Settings page allows CRUD on provider configs
- [ ] Vehicle mapping page works
- [ ] `npm run build` passes with zero TypeScript errors
- [ ] "GPS Tracker" visible in Fleet section of sidebar
