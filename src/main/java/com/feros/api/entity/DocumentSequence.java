package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_sequences",
    uniqueConstraints = @UniqueConstraint(name = "uk_doc_seq", columnNames = {"tenant_id", "doc_type", "period"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "doc_type", nullable = false, length = 20)
    private String docType;

    @Column(name = "period", nullable = false, length = 10)
    private String period;

    @Column(name = "last_seq", nullable = false)
    private Long lastSeq;
}
