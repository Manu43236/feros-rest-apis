package com.feros.api.entity;

import com.feros.api.entity.master.City;
import com.feros.api.entity.master.MaterialType;
import com.feros.api.entity.master.Route;
import com.feros.api.entity.master.State;
import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import com.feros.api.enums.OrderPaymentStatus;
import com.feros.api.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_tenant_date",   columnList = "tenant_id, order_date"),
    @Index(name = "idx_orders_tenant_status", columnList = "tenant_id, order_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_type_id", nullable = false)
    private MaterialType materialType;

    @Column(name = "total_weight", nullable = false)
    private BigDecimal totalWeight;

    @Column(name = "total_weight_fulfilled")
    private BigDecimal totalWeightFulfilled = BigDecimal.ZERO;

    @Column(name = "source_address", columnDefinition = "TEXT")
    private String sourceAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_city_id", nullable = false)
    private City sourceCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_state_id", nullable = false)
    private State sourceState;

    @Column(name = "destination_address", columnDefinition = "TEXT")
    private String destinationAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private City destinationCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_state_id", nullable = false)
    private State destinationState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(name = "freight_rate_type", nullable = false)
    private FreightRateType freightRateType;

    @Column(name = "freight_rate", nullable = false)
    private BigDecimal freightRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_on")
    private BillingOn billingOn = BillingOn.LOADED_WEIGHT;

    @Column(name = "total_freight_amount")
    private BigDecimal totalFreightAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_payment_status")
    private OrderPaymentStatus orderPaymentStatus = OrderPaymentStatus.UNPAID;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_pol")
    private Boolean isPol = false;

    @Column(name = "is_active")
    private Boolean isActive = true;
}