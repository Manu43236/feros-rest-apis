package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientDivisionResponse {
    private Long id;
    private String name;
}
