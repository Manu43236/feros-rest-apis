package com.feros.api.entity;

import com.feros.api.entity.master.MaterialType;
import com.feros.api.entity.master.Unit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * One cargo-manifest line on a multi-material order: a material + quantity + unit.
 * Purely additive — only populated when the order has {@code isMultiMaterial = true}.
 * Single-material orders have zero rows and fall back to {@code Order.materialType}/{@code totalWeight}.
 * Line 1 mirrors the order's primary material/weight so every existing read stays intact.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order",  columnList = "order_id"),
    @Index(name = "idx_order_items_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_type_id", nullable = false)
    private MaterialType materialType;        // custom "Other" names resolve into a MaterialType row

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
