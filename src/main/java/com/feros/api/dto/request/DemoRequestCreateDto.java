package com.feros.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoRequestCreateDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Company is required")
    @Size(max = 150)
    private String company;

    @Size(max = 150)
    private String email;

    @Size(max = 50)
    private String fleetSize;

    @Size(max = 100)
    private String city;
}
