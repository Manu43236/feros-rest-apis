package com.feros.api.entity;

import com.feros.api.enums.ServiceAttachmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_service_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleServiceAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private VehicleService service;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ServiceAttachmentType type;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "uploaded_at")
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
