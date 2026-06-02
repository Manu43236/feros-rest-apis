package com.feros.api.entity;

import com.feros.api.enums.AssignmentEventType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_events", indexes = {
    @Index(name = "idx_ae_vehicle_tenant", columnList = "vehicle_id, tenant_id"),
    @Index(name = "idx_ae_order_tenant",   columnList = "order_id, tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AssignmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "vehicle_registration_number", nullable = false)
    private String vehicleRegistrationNumber;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AssignmentEventType eventType;

    /** Driver / cleaner name — null for vehicle-level events */
    @Column(name = "person_name")
    private String personName;

    /** DRIVER / CLEANER / etc — null for vehicle-level events */
    @Column(name = "person_role")
    private String personRole;

    @Column(name = "performed_by_id", nullable = false)
    private Long performedById;

    @Column(name = "performed_by_name", nullable = false)
    private String performedByName;

    @CreatedDate
    @Column(name = "performed_at", updatable = false)
    private LocalDateTime performedAt;
}
