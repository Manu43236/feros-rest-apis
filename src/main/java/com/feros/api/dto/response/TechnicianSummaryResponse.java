package com.feros.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianSummaryResponse {
    private Long id;
    private String name;
    private String designation;
}
