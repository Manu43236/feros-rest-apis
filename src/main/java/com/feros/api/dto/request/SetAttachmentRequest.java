package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetAttachmentRequest {
    // null clears the attachment from the machine line
    private Long attachmentId;
}
