package com.feros.api.controller;

import com.feros.api.dto.request.CreateUserRequest;
import com.feros.api.dto.request.UserStatusRequest;
import com.feros.api.dto.response.ApiResponse;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.PinResponse;
import com.feros.api.dto.response.UserResponse;
import com.feros.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(
                ApiResponse.success("User created successfully", response)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", response)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", response)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", response)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusRequest request) {
        UserResponse response = userService.toggleUserStatus(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("User status updated successfully", response)
        );
    }

    @PutMapping("/{id}/reset-pin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PinResponse>> resetPin(
            @PathVariable Long id) {
        PinResponse response = userService.resetPin(id);
        return ResponseEntity.ok(
                ApiResponse.success("PIN reset successfully", response)
        );
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkTenantUploadResponse>> bulkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tenantId", required = false) Long tenantId) {
        BulkTenantUploadResponse response = userService.bulkUpload(file, tenantId);
        return ResponseEntity.ok(
                ApiResponse.success("Bulk upload completed", response)
        );
    }

    @PostMapping("/staff-bulk-upload")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkTenantUploadResponse>> staffBulkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tenantId", required = false) Long tenantId) {
        BulkTenantUploadResponse response = userService.staffBulkUpload(file, tenantId);
        return ResponseEntity.ok(
                ApiResponse.success("Staff bulk upload completed", response)
        );
    }
}