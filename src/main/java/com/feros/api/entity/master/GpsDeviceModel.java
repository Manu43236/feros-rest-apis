package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.enums.GpsConnectionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gps_device_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsDeviceModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false)
    private GpsConnectionType connectionType;

    @Column(name = "parser_key", nullable = false, unique = true)
    private String parserKey;

    @Column(name = "protocol_version")
    private String protocolVersion;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
