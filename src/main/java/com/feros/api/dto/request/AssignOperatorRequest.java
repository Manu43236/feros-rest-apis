package com.feros.api.dto.request;

import com.feros.api.enums.OperatorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignOperatorRequest {
    private OperatorType operatorType; // null = remove operator
    private Long operatorStaffId;      // for OWN_STAFF
    private String hiredOperatorName;  // for HIRED
    private String hiredOperatorPhone; // for HIRED (optional)
}
