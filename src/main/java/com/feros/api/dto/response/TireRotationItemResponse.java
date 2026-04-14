package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TireRotationItemResponse {
    private Long id;
    private Long tireId;
    private String tireSerialNumber;
    private Long fromPositionId;
    private String fromPositionCode;
    private Long toPositionId;
    private String toPositionCode;
    private Long oldFittingId;
    private Long newFittingId;
}
