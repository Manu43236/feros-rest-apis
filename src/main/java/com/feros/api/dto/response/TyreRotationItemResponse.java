package com.feros.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TyreRotationItemResponse {
    private Long id;
    private Long tyreId;
    private String tyreSerialNumber;
    private Long fromPositionId;
    private String fromPositionCode;
    private Long toPositionId;
    private String toPositionCode;
    private Long oldFittingId;
    private Long newFittingId;
}
