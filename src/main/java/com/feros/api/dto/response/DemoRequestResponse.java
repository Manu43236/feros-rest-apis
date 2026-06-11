package com.feros.api.dto.response;

import com.feros.api.entity.DemoRequest;
import com.feros.api.enums.DemoRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DemoRequestResponse {
    private Long id;
    private String name;
    private String phone;
    private String company;
    private String email;
    private String fleetSize;
    private String city;
    private DemoRequestStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DemoRequestResponse from(DemoRequest d) {
        return DemoRequestResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .phone(d.getPhone())
                .company(d.getCompany())
                .email(d.getEmail())
                .fleetSize(d.getFleetSize())
                .city(d.getCity())
                .status(d.getStatus())
                .notes(d.getNotes())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
