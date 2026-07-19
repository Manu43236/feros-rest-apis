CREATE TABLE IF NOT EXISTS supervisor_vehicle_watchlist (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    supervisor_id BIGINT NOT NULL,
    vehicle_id    BIGINT NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_sup_vehicle (tenant_id, supervisor_id, vehicle_id),
    FOREIGN KEY (tenant_id)     REFERENCES tenants(id),
    FOREIGN KEY (supervisor_id) REFERENCES users(id),
    FOREIGN KEY (vehicle_id)    REFERENCES vehicles(id)
);

CREATE TABLE IF NOT EXISTS supervisor_staff_watchlist (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    supervisor_id BIGINT NOT NULL,
    user_id       BIGINT NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_sup_staff (tenant_id, supervisor_id, user_id),
    FOREIGN KEY (tenant_id)     REFERENCES tenants(id),
    FOREIGN KEY (supervisor_id) REFERENCES users(id),
    FOREIGN KEY (user_id)       REFERENCES users(id)
);
