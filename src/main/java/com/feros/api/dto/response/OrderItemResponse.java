package com.feros.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Integer lineNo;
    private Long materialTypeId;
    private String materialName;
    private BigDecimal quantity;
    private Long unitId;
    private String unitName;
}
