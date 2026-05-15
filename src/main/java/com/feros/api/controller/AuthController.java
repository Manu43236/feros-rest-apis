package com.feros.api.controller;

import com.feros.api.dto.request.ChangePinRequest;
import com.feros.api.dto.request.LoginRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.LoginResponse;
import com.feros.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }

    @PatchMapping("/change-pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePin(
            @Valid @RequestBody ChangePinRequest request) {
        authService.changePin(request);
        return ResponseEntity.ok(ApiResponse.success("PIN changed successfully", null));
    }

    @GetMapping("/hash/{pin}")
    public String hashPin(@PathVariable String pin) {
        return passwordEncoder.encode(pin);
    }
}