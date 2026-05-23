package com.feros.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkActionRequest {
    @NotEmpty(message = "IDs list cannot be empty")
    private List<Long> ids;
}
