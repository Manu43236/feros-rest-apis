package com.feros.api.dto.request;

import com.feros.api.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DesignationRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Role type is required")
    private RoleName roleType;

    private BigDecimal payPerDay;

    private Integer monthlyLeaveQuota;
}