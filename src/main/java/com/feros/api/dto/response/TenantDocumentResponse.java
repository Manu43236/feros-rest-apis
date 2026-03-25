package com.feros.api.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDocumentResponse {
    private Long id;
    private String documentName;
    private String originalFileName;
    private String s3Key;
    private String fileUrl;        // presigned URL (1 hour validity)
    private LocalDateTime createdAt;
}
