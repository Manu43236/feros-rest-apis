package com.feros.api.dto.response;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinResponse {
    private Long userId;
    private String name;
    private String phone;
    private String pin;
    private String message;
}