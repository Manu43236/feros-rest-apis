package com.feros.api.dto.request;

import com.feros.api.enums.ApplicableFor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentTypeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Applicable for is required")
    private ApplicableFor applicableFor;

    private java.util.List<String> applicableRoles;
}