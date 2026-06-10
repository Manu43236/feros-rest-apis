package com.feros.api.entity;

import com.feros.api.enums.TyrePurchaseCondition;
import com.feros.api.enums.TyreStatus;
import com.feros.api.enums.TyreType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tyres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tyre extends BaseEntity {

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

    @Column(name = "tyre_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TyreType tyreType;

    @Column(name = "ply_rating")
    private String plyRating;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 12, scale = 2)
    private BigDecimal purchaseCost;

    @Builder.Default
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TyreStatus status = TyreStatus.IN_STOCK;

    @Builder.Default
    @Column(name = "retread_count", nullable = false)
    private Integer retreadCount = 0;

    @Builder.Default
    @Column(name = "total_lifetime_km", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalLifetimeKm = BigDecimal.ZERO;

    @Column(name = "tyre_life_years")
    private Integer tyreLifeYears;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "max_lifetime_km", precision = 12, scale = 2)
    private BigDecimal maxLifetimeKm;

    @Builder.Default
    @Column(name = "last_km_alert_km", precision = 12, scale = 2)
    private BigDecimal lastKmAlertKm = BigDecimal.ZERO;

    @Column(name = "retreader_name")
    private String retreaderName;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Purchase condition — NEW / SECOND_HAND / RETREADED
    @Builder.Default
    @Column(name = "purchase_condition", nullable = false)
    @Enumerated(EnumType.STRING)
    private TyrePurchaseCondition purchaseCondition = TyrePurchaseCondition.NEW;

    // For second-hand/retreaded tyres: KM already on the tyre when purchased
    @Builder.Default
    @Column(name = "km_at_purchase", precision = 12, scale = 2)
    private BigDecimal kmAtPurchase = BigDecimal.ZERO;

    // Cumulative retreading cost across all retread cycles
    @Builder.Default
    @Column(name = "total_retreading_cost", precision = 12, scale = 2)
    private BigDecimal totalRetreadingCost = BigDecimal.ZERO;

    // Reason and date when directly scrapped (without vehicle removal)
    @Column(name = "scrap_reason")
    private String scrapReason;

    @Column(name = "scrap_date")
    private LocalDate scrapDate;

    // Purchase reference
    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
