package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spare_parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "category")
    private String category;

    @Column(name = "unit", nullable = false)
    @Builder.Default
    private String unit = "Pieces";

    @Column(name = "min_stock_level", nullable = false)
    @Builder.Default
    private Integer minStockLevel = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
