package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DocumentListRow {
    private Long documentId;
    private String entityType;      // VEHICLE | STAFF
    private Long ownerId;           // vehicleId or userId
    private String ownerName;       // registration number or staff name
    private String ownerSubtitle;   // vehicle type or staff role
    private Long documentTypeId;
    private String documentTypeName;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String fileUrl;
    private Long daysLeft;          // null when no expiry date
    private String status;          // EXPIRED | EXPIRING | ACTIVE
}
