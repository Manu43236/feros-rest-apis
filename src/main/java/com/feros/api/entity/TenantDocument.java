package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenant_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
