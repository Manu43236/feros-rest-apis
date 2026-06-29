package com.feros.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssignDivisionRequest {
    private Long divisionId; // null = remove division
}
