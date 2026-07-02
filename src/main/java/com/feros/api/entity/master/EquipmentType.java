package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.enums.MeterType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "equipment_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private EquipmentModel model;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_meter_type", nullable = false)
    private MeterType defaultMeterType;

    @Column(name = "capacity", precision = 10, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 10)
    private String capacityUnit;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
