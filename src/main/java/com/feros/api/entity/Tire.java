package com.feros.api.entity;

import com.feros.api.enums.TireStatus;
import com.feros.api.enums.TireType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tire extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "size", nullable = false)
    private String size;

    @Column(name = "tire_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TireType tireType;

    @Column(name = "ply_rating")
    private String plyRating;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 12, scale = 2)
    private BigDecimal purchaseCost;

    @Builder.Default
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TireStatus status = TireStatus.IN_STOCK;

    @Builder.Default
    @Column(name = "retread_count", nullable = false)
    private Integer retreadCount = 0;

    @Builder.Default
    @Column(name = "total_lifetime_km", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalLifetimeKm = BigDecimal.ZERO;

    @Column(name = "retreader_name")
    private String retreaderName;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
