package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.enums.VehicleStatusType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "status_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleStatusType statusType;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
