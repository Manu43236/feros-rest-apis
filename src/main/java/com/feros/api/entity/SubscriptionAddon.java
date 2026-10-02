package com.feros.api.entity;

import com.feros.api.util.TimeUtil;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A mid-cycle add-on of extra vehicle slots on top of a tenant's active subscription.
 * Billed pro-rata for the remaining period and paid immediately. Counts toward the
 * effective slot limit while its parent {@link SubscriptionHistory} is ACTIVE.
 */
@Entity
@Table(name = "subscription_addons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // The base subscription this add-on extends; the link that makes it "active".
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_history_id", nullable = false)
    private SubscriptionHistory subscriptionHistory;

    @Column(name = "addon_vehicle_count", nullable = false)
    private Integer addonVehicleCount;

    @Column(name = "price_per_vehicle", precision = 10, scale = 2)
    private BigDecimal pricePerVehicle;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "gst_amount", precision = 10, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_ref")
    private String paymentRef;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "status")
    private String status; // ACTIVE (reserved for future remove/expire)

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = TimeUtil.nowIst(); }
}
