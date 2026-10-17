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

    // Party details (add-on). `client` above = the placing client (who booked the order).
    // All three below are nullable. When empty: invoice bills the placing client and the LR
    // falls back to legacy behaviour (consignee = client, consignor = source city).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignor_client_id")
    private Client consignorClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignee_client_id")
    private Client consigneeClient;

    // Who the invoice is billed to. Null → bill the placing `client`.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_party_client_id")
    private Client billingPartyClient;

    // Effective billing party = explicit billing client, else the placing client.
    @Transient
    public Client getEffectiveBillingClient() {
        return billingPartyClient != null ? billingPartyClient : client;
    }

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

    @Column(name = "is_multi_material")
    private Boolean isMultiMaterial = false;

    @Column(name = "is_active")
    private Boolean isActive = true;
}