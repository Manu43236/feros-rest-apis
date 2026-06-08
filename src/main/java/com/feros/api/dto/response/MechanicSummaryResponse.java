package com.feros.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MechanicSummaryResponse {
    private Long id;
    private String name;
    private String phone;
    private String userNumber;
}
