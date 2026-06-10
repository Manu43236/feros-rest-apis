package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tyre_retread_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TyreRetreadLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tyre_id", nullable = false)
    private Tyre tyre;

    @Column(name = "retread_number", nullable = false)
    private Integer retreadNumber;

    @Column(name = "sent_date")
    private LocalDate sentDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "retreader_name")
    private String retreaderName;

    @Column(name = "km_at_send", precision = 12, scale = 2)
    private BigDecimal kmAtSend;

    @Column(name = "retreading_cost", precision = 12, scale = 2)
    private BigDecimal retreadingCost;

    @Column(name = "new_max_lifetime_km", precision = 12, scale = 2)
    private BigDecimal newMaxLifetimeKm;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
