package com.feros.api.dto.response;

import com.feros.api.enums.ServiceAttachmentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAttachmentResponse {
    private Long id;
    private ServiceAttachmentType type;
    private String url;
    private LocalDateTime uploadedAt;
}
