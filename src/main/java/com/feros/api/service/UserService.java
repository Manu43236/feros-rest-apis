package com.feros.api.service;

import com.feros.api.dto.request.CreateUserRequest;
import com.feros.api.dto.request.UserStatusRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.PinResponse;
import com.feros.api.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers(Boolean hasAttendanceToday);

    UserResponse updateUser(Long id, CreateUserRequest request);

    void deleteUser(Long id);

    PinResponse resetPin(Long id);

    BulkTenantUploadResponse bulkUpload(MultipartFile file, Long tenantId);

    BulkTenantUploadResponse staffBulkUpload(MultipartFile file, Long tenantId);

    UserResponse toggleUserStatus(Long userId, UserStatusRequest request);
}