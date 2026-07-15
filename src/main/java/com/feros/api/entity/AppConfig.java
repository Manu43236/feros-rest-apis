package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_version", nullable = false)
    private Integer minVersion;

    @Column(name = "latest_version", nullable = false)
    private Integer latestVersion;

    // manual override: force immediate update for all users regardless of version
    @Column(name = "force_update", nullable = false)
    private Boolean forceUpdate;
}
