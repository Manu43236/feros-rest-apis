package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdleDriverResponse {
    private Long userId;
    private String userName;
    private String phone;
    private String roleName;
}
