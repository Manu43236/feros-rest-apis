-- GPS Integration tables (reference only — Hibernate manages schema via ddl-auto=update)

CREATE TABLE gps_provider_configs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id         BIGINT NOT NULL,
    provider_type     VARCHAR(50) NOT NULL,
    display_name      VARCHAR(100),
    client_id_enc     TEXT NOT NULL,
    client_secret_enc TEXT NOT NULL,
    api_base_url      VARCHAR(255),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    last_sync_at      DATETIME,
    sync_status       VARCHAR(20) DEFAULT 'NEVER',
    sync_error_msg    TEXT,
    created_at        DATETIME,
    updated_at        DATETIME,
    CONSTRAINT fk_gps_config_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE TABLE vehicle_gps_mappings (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    vehicle_id              BIGINT NOT NULL,
    gps_provider_config_id  BIGINT NOT NULL,
    provider_vehicle_id     VARCHAR(100) NOT NULL,
    provider_reg_number     VARCHAR(50),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              DATETIME,
    updated_at              DATETIME,
    CONSTRAINT fk_gps_map_tenant  FOREIGN KEY (tenant_id)             REFERENCES tenants(id),
    CONSTRAINT fk_gps_map_vehicle FOREIGN KEY (vehicle_id)            REFERENCES vehicles(id),
    CONSTRAINT fk_gps_map_config  FOREIGN KEY (gps_provider_config_id) REFERENCES gps_provider_configs(id),
    CONSTRAINT uq_vehicle_gps_mapping UNIQUE (vehicle_id, gps_provider_config_id)
);
