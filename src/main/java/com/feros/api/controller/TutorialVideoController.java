package com.feros.api.controller;

import com.feros.api.dto.request.TutorialVideoRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.TutorialVideoResponse;
import com.feros.api.service.TutorialVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tutorial-videos")
@RequiredArgsConstructor
public class TutorialVideoController {

    private final TutorialVideoService service;

    // mobile — authenticated, role + language filtered
    @GetMapping
    public ResponseEntity<ApiResponse<List<TutorialVideoResponse>>> getForMobile(
            @RequestParam(defaultValue = "te") String language) {
        return ResponseEntity.ok(ApiResponse.success("Tutorial videos fetched", service.getForMobile(language)));
    }

    // SA — full unfiltered list
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TutorialVideoResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("All tutorial videos fetched", service.getAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TutorialVideoResponse>> create(@RequestBody TutorialVideoRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tutorial video created", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TutorialVideoResponse>> update(
            @PathVariable Long id, @RequestBody TutorialVideoRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tutorial video updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tutorial video deleted", null));
    }
}
