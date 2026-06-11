package com.feros.api.entity;

import com.feros.api.enums.DemoRequestStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "demo_requests")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DemoRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(length = 150)
    private String email;

    @Column(name = "fleet_size", length = 50)
    private String fleetSize;

    @Column(length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DemoRequestStatus status = DemoRequestStatus.NEW;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
