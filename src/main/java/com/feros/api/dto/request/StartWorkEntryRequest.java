package com.feros.api.dto.request;

import com.feros.api.enums.OperatorType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StartWorkEntryRequest {

    @NotNull(message = "Operator type is required")
    private OperatorType operatorType;

    private Long operatorStaffId;      // for OWN_STAFF
    private String hiredOperatorName;  // for HIRED

    @NotNull(message = "Start meter reading is required")
    private BigDecimal startMeter;
}
