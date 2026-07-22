package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HolidayRequest {

    @NotNull(message = "Date is required")
    private LocalDate holidayDate;

    @NotBlank(message = "Name is required")
    private String holidayName;
}
