package com.feros.api.entity;

import com.feros.api.enums.FuelPaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment_fuel_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentFuelLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "fill_date", nullable = false)
    private LocalDate fillDate;

    @Column(name = "litres_filled", nullable = false, precision = 10, scale = 2)
    private BigDecimal litresFilled;

    @Column(name = "hmr_at_fill", precision = 10, scale = 2)
    private BigDecimal hmrAtFill;

    @Column(name = "cost_per_litre", precision = 10, scale = 2)
    private BigDecimal costPerLitre;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "is_full_tank")
    @Builder.Default
    private Boolean isFullTank = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    private FuelPaymentMode paymentMode;

    @Column(name = "fuel_station")
    private String fuelStation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
