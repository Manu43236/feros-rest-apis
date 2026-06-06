package com.feros.api.entity;

import com.feros.api.enums.GpsProviderType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gps_provider_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsProviderConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 50)
    private GpsProviderType providerType;

    @Column(name = "display_name", length = 100)
    private String displayName;

    // AES-256-GCM encrypted values — never store plain text
    @Column(name = "client_id_enc", nullable = false, columnDefinition = "TEXT")
    private String clientIdEnc;

    @Column(name = "client_secret_enc", nullable = false, columnDefinition = "TEXT")
    private String clientSecretEnc;

    @Column(name = "api_base_url", length = 255)
    private String apiBaseUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_sync_at")
    private java.time.LocalDateTime lastSyncAt;

    @Column(name = "sync_status", length = 20)
    @Builder.Default
    private String syncStatus = "NEVER";

    @Column(name = "sync_error_msg", columnDefinition = "TEXT")
    private String syncErrorMsg;
}
