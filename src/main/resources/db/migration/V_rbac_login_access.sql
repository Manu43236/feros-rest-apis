CREATE TABLE IF NOT EXISTS rbac_login_access (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    role        VARCHAR(30)  NOT NULL,
    platform    VARCHAR(10)  NOT NULL,
    allowed     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_role_platform (tenant_id, role, platform),
    CONSTRAINT fk_rbac_login_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);
