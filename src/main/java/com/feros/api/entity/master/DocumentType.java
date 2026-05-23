package com.feros.api.entity.master;

import com.feros.api.entity.BaseEntity;
import com.feros.api.enums.ApplicableFor;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_for", nullable = false)
    private ApplicableFor applicableFor;

    @Column(name = "applicable_roles", columnDefinition = "JSON")
    private String applicableRoles;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "allow_multiple", nullable = false)
    private Boolean allowMultiple = true;
}