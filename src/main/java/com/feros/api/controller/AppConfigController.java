package com.feros.api.controller;

import com.feros.api.dto.request.AppConfigRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.AppConfigResponse;
import com.feros.api.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService appConfigService;

    // public — mobile calls this before login
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<AppConfigResponse>> get() {
        return ResponseEntity.ok(ApiResponse.success("App config fetched", appConfigService.get()));
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppConfigResponse>> update(@RequestBody AppConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success("App config updated", appConfigService.update(request)));
    }
}
