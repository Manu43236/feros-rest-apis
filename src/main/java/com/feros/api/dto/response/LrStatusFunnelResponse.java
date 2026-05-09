package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LrStatusFunnelResponse {
    private int created;
    private int inTransit;
    private int delivered;
    private int cancelled;
    private int total;
}
